package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkSnapshot;
import io.github.zap.vector.Vector2I;
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

    private static final int MIN_CHUNK_SYNC_AGE = 40;
    private static final int PATH_CAPACITY = 32;
    private static final int MAX_SCHEDULED_SYNC_TASKS = 2;
    private static final double PERCENTAGE_STALE_REQUIRED_TO_FORCE = 0.9;
    private static final int CHUNK_SYNC_TIMEOUT_MS = 1000;

    private final ExecutorCompletionService<PathResult> completionService =
            new ExecutorCompletionService<>(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));

    private final Map<UUID, Context> contexts = new ConcurrentHashMap<>();

    private class Context implements PathfinderContext {
        private final Semaphore syncSemaphore = new Semaphore(MAX_SCHEDULED_SYNC_TASKS);

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

            if (state == PathOperation.State.SUCCEEDED) {
                handleAddition(path, successfulPaths);
            }
        }

        private void handleAddition(PathResult result, Queue<PathResult> target) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
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
            exception.printStackTrace();

            ArenaApi.warning("Cause: " + exception.getCause());
        }
        return null;
    }

    //fancy path merging optimizations; this code alone results in extremely significant speedups
    private PathResult attemptMergeFor(Context context, PathOperation operation) {
        synchronized (context.successfulPaths) { //try to merge paths if possible
            PathNode currentNode = operation.currentNode();

            if(currentNode != null) {
                for(PathResult successful : context.successfulPaths) {
                    ChunkGraph<PathNode> resultVisited = successful.visitedNodes(); //get nodes visited by another path

                    int x = currentNode.x();
                    int y = currentNode.y();
                    int z = currentNode.z();

                    //check if we can merge
                    if(operation.mergeValid(successful.operation()) && resultVisited.hasElementAt(x, y, z)) {
                        PathNode intersection = resultVisited.elementAt(x, y, z); //get intersection point

                        PathNode sample = intersection;
                        while(sample != null) {
                            PathNode parent = sample.parent; //iterate up parents

                            if(parent != null && parent.child != sample) { //check for "broken" link (indicative of path)
                                PathNode oldIntersectionChild = intersection.child;

                                intersection.child = currentNode.child;
                                if(currentNode.child != null) {
                                    currentNode.child.parent = intersection; //link up paths
                                }

                                parent.child = sample; //point path back towards origin

                                PathNode first = intersection;
                                while(first.child != null) { //get origin node
                                    first = first.child;
                                }

                                PathDestination destination = operation.bestDestination(); //we can just stop now
                                if(destination != null) {
                                    ArenaApi.info("Merged paths (based on common explored node)");
                                    return new PathResultImpl(first, operation, resultVisited, destination,
                                            PathOperation.State.SUCCEEDED);
                                }

                                intersection.child = oldIntersectionChild;
                                return null;
                            }

                            sample = sample.parent;
                        }

                        PathDestination destination = operation.bestDestination();
                        if(intersection != null && destination != null) {
                            //if we reach this point, it means we started directly on an existing path
                            ArenaApi.info("Merged paths (we're standing on a path already)");
                            return new PathResultImpl(intersection, operation, resultVisited, destination,
                                    PathOperation.State.SUCCEEDED);
                        }
                    }
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

        for(Vector2I chunkVectorAccess : chunks) {
            CollisionChunkSnapshot chunk = provider.chunkAt(chunkVectorAccess.x(), chunkVectorAccess.z());

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
                    ArenaApi.warning("Interrupted while attempting to force acquire synchronization semaphore.");
                    return;
                }
            }

            CountDownLatch latch = new CountDownLatch(1);
            int taskId = Bukkit.getScheduler().runTask(ArenaApi.getInstance(), () -> {
                try {
                    context.blockCollisionProvider.updateRegion(coordinateProvider);
                }
                catch (Exception exception) {
                    ArenaApi.warning("An exception occurred while synchronizing chunks:");
                    exception.printStackTrace();
                }
                finally {
                    context.lastSyncTick = Bukkit.getCurrentTick();
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
                    latch.countDown(); //idk if necessary
                }
            } catch (InterruptedException ignored) {
                ArenaApi.warning("Interrupted while waiting for chunks to sync.");
                Bukkit.getScheduler().cancelTask(taskId);
            }
        }
    }

    /**
     * Queues a pathfinding operation onto the pathfinding thread. This method is thread-safe.
     * @param operation The operation to enqueue
     * @return A Future object representing the computation
     */
    @Override
    public @NotNull Future<PathResult> giveOperation(@NotNull PathOperation operation, @NotNull World world) {
        Context context = contexts.computeIfAbsent(world.getUID(), (key) ->
                new Context(new AsyncBlockCollisionProvider(world, MIN_CHUNK_SYNC_AGE)));

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
        Context context = contexts.get(event.getWorld().getUID());

        if(context != null) {
            context.blockProvider().clearForWorld();
        }
    }
}
