package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.nms.common.world.CollisionChunkSnapshot;
import io.github.zap.vector.ChunkVectorAccess;
import io.github.zap.vector.graph.ChunkGraph;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;

public class AsyncPathfinderEngine implements PathfinderEngine, Listener {
    private static final AsyncPathfinderEngine INSTANCE = new AsyncPathfinderEngine();

    private static final int MIN_CHUNK_SYNC_AGE = 20;
    private static final int PATH_CAPACITY = 128;
    private static final int MAX_SCHEDULED_SYNC_TASKS = 4;
    private static final double PERCENTAGE_STALE_REQUIRED_TO_FORCE = 0.7;
    private static final int CHUNK_SYNC_TIMEOUT_MS = 1000;

    private final ExecutorCompletionService<PathResult> completionService =
            new ExecutorCompletionService<>(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));

    private final Map<UUID, Context> contexts = new HashMap<>();

    private class Context implements PathfinderContext {
        private final Semaphore syncSemaphore = new Semaphore(MAX_SCHEDULED_SYNC_TASKS);
        private final Object contextSyncHandle = new Object();

        private final Queue<PathResult> successfulPaths = new ArrayDeque<>();
        private final BlockCollisionProvider blockCollisionProvider;

        private int lastSyncTick = -1;

        private Context(@NotNull BlockCollisionProvider blockCollisionProvider) {
            this.blockCollisionProvider = blockCollisionProvider;
        }

        @Override
        public @NotNull PathfinderEngine engine() {
            return AsyncPathfinderEngine.this;
        }

        @Override
        public @NotNull BlockCollisionProvider blockProvider() {
            return blockCollisionProvider;
        }

        public void recordPath(PathResult path) {
            PathOperation.State state = path.state();

            switch (state) {
                case SUCCEEDED -> handleAddition(path, successfulPaths);
            }
        }

        private void handleAddition(PathResult result, Queue<PathResult> target) {
            synchronized (target) {
                int oldCount = target.size();
                int newCount = oldCount + 1;

                target.add(result);

                if(newCount == PATH_CAPACITY) {
                    target.poll();
                }
            }
        }
    }

    private AsyncPathfinderEngine() { //singleton
        Bukkit.getServer().getPluginManager().registerEvents(this, ArenaApi.getInstance());
    }

    private PathResult processOperation(Context context, PathOperation operation) {
        try {
            trySyncChunks(context, operation.searchArea(), isUrgent(operation.searchArea(), context.blockProvider()));

            operation.init(context);

            while(operation.state() == PathOperation.State.STARTED) {
                for(int i = 0; i < operation.iterations(); i++) {
                    if(operation.step(context)) {
                        PathResult result = operation.result();
                        context.recordPath(result);
                        return result;
                    }
                }

                if(Thread.interrupted()) {
                    ArenaApi.warning("processOperation interrupted for PathOperation. Returning null PathResult.");
                    return null;
                }

                if(operation.allowMerges()) {
                    PathResult result = attemptMergeFor(context, operation);

                    if(result != null) {
                        return result;
                    }
                }
            }

            return operation.result();
        }
        catch (Exception exception) {
            ArenaApi.warning("Exception thrown in PathOperation handler:");
            exception.fillInStackTrace().printStackTrace();

            ArenaApi.warning("Cause: " + exception.getCause());
        }

        return null;
    }

    //fancy path merging optimizations; this code alone results in extremely significant speedups
    private PathResult attemptMergeFor(Context context, PathOperation operation) {
        synchronized (context.successfulPaths) { //try to merge paths if possible
            PathNode currentNode = operation.currentNode();

            for(PathResult successful : context.successfulPaths) {
                ChunkGraph<PathNode> resultVisited = successful.visitedNodes();

                int x = currentNode.nodeX();
                int y = currentNode.nodeY();
                int z = currentNode.nodeZ();

                if(operation.mergeValid(successful.operation()) && resultVisited.hasElement(x, y, z)) {
                    PathNode intersection = resultVisited.elementAt(x, y, z);

                    PathNode current = intersection;
                    PathNode lastNode = null;

                    while(current != null) {
                        PathNode parent = current.parent;

                        if(parent != null) {
                            if(lastNode != parent) { //we found a break; indicative of the previous path
                                intersection.parent = currentNode.parent; //make sure intersection fits in with our explored nodes
                                if(currentNode.parent != null) {
                                    currentNode.parent.child = intersection;
                                }

                                PathNode tail = current;
                                while(tail.child != null) { //find tail (should be previous origin)
                                    tail = tail.child;
                                }

                                current.parent = null; //set parent to null so we don't reverse the whole path
                                tail.reverse(); //reverse our old path to maintain data integrity
                                current.parent = lastNode; //set parent going the other way, towards our origin

                                PathNode start = current.reverse(); //set current node going towards the shared destination
                                current.parent = parent; //set our old parent again

                                return new PathResultImpl(start, successful.operation(), successful.visitedNodes(),
                                        successful.destination(), PathOperation.State.SUCCEEDED);
                            }
                        }

                        lastNode = current; //keep track of previous node explored
                        current = current.child; //iterate through children
                    }

                    //if we reach this point, it means we started directly on an existing path
                    return new PathResultImpl(intersection, successful.operation(), successful.visitedNodes(),
                            successful.destination(), PathOperation.State.SUCCEEDED);
                }
            }
        }

        return null;
    }

    /**
     * Simple algorithm to quickly evaluate if the percentage of chunks for a given provider that are either outdated or
     * not present is high enough to warrant forcing a chunk sync
     */
    private boolean isUrgent(ChunkCoordinateProvider chunks, BlockCollisionProvider provider) {
        int stale = 0;

        for(ChunkVectorAccess chunkVectorAccess : chunks) {
            CollisionChunkSnapshot chunk = provider.chunkAt(chunkVectorAccess.chunkX(), chunkVectorAccess.chunkZ());

            if(chunk == null || (Bukkit.getCurrentTick() - chunk.captureTick()) > MIN_CHUNK_SYNC_AGE) {
                stale++;
            }
        }

        return (double)stale / (double)chunks.chunkCount() >= PERCENTAGE_STALE_REQUIRED_TO_FORCE;
    }

    private void trySyncChunks(Context context, ChunkCoordinateProvider coordinateProvider, boolean force) {
        int currentTick = Bukkit.getCurrentTick();

        /*
        defer to threads that have already scheduled synchronization on this context, up to a maximum of
        MAX_SCHEDULED_SYNC_TASKS. this is to avoid overloading the fragile BukkitScheduler (and the main thread), as
        well as avoiding potentially redundant sync attempts

        or, if we're forcing, schedule the sync no matter what. this is generally used in cases where the context
        really, really needs fresh chunks, such as for a new PathOperation or one that's going on in a really outdated
        area
        */
        if(force || (currentTick - context.lastSyncTick) > MIN_CHUNK_SYNC_AGE && context.syncSemaphore.tryAcquire()) {
            if(force) {
                /*
                 * respect the hard limit of bukkit tasks, but we really need this operation to complete, so wait
                 * to acquire it
                 */
                try {
                    context.syncSemaphore.acquire();
                }
                catch (InterruptedException e) {
                    ArenaApi.warning("Interrupted while attemping to force acquire sychronization semaphore.");
                    return;
                }
            }

            CountDownLatch latch = new CountDownLatch(1);
            int taskId = Bukkit.getScheduler().runTask(ArenaApi.getInstance(), () -> {
                boolean noErr = true;
                try {
                    context.blockCollisionProvider.updateRegion(coordinateProvider);
                    context.lastSyncTick = currentTick;
                }
                catch (Exception exception) {
                    ArenaApi.warning("An exception occurred while synchronizing chunks:");
                    exception.printStackTrace();
                    noErr = false;
                }
                finally {
                    context.syncSemaphore.release();
                    latch.countDown();
                }
            }).getTaskId();

            //wait for the sync to complete
            try {
                if(force) {
                    latch.await(); //await forever if forced, to ensure pathing in urgent cases is not premature
                } else if(!latch.await(CHUNK_SYNC_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                    ArenaApi.warning("Chunk synchronizing took more than " + CHUNK_SYNC_TIMEOUT_MS + "ms! Is the server lagging?");
                    Bukkit.getScheduler().cancelTask(taskId); //don't bother to finish sync on a laggy server
                }
            } catch (InterruptedException ignored) {
                ArenaApi.warning("Interrupted while waiting for chunks to sync.");
                Bukkit.getScheduler().cancelTask(taskId);
            }
        }

        return;
    }

    /**
     * Queues a pathfinding operation onto the pathfinding thread. This method is thread-safe.
     * @param operation The operation to enqueue
     * @return A Future object representing the computation
     */
    @Override
    public @NotNull Future<PathResult> giveOperation(@NotNull PathOperation operation, @NotNull World world) {
        Context context;
        synchronized (contexts) {
            context = contexts.computeIfAbsent(world.getUID(), (key) ->
                    new Context(new AsyncBlockCollisionProvider(world, MIN_CHUNK_SYNC_AGE)));
        }

        return completionService.submit(() -> processOperation(context, operation));
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    public static AsyncPathfinderEngine instance() {
        return INSTANCE;
    }

    @EventHandler
    private void onWorldUnload(WorldUnloadEvent event) {
        synchronized (contexts) {
            Context context = contexts.get(event.getWorld().getUID());

            if(context != null) {
                context.blockProvider().clearForWorld();
            }
        }
    }
}