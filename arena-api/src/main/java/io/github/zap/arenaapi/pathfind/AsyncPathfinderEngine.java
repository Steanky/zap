package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.nms.common.world.CollisionChunkSnapshot;
import io.github.zap.vector.ChunkVectorAccess;
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
    private static final int MAX_SCHEDULED_SYNC_TASKS = 4;
    private static final double URGENT_SYNC_THRESHOLD = 0.50;
    private static final int CHUNK_SYNC_TIMEOUT_MS = 1000;

    private final ExecutorCompletionService<PathResult> completionService =
            new ExecutorCompletionService<>(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));

    private final Map<UUID, Context> contextMap = new ConcurrentHashMap<>();

    private class Context implements PathfinderContext {
        private final Semaphore syncSemaphore = new Semaphore(MAX_SCHEDULED_SYNC_TASKS);
        private final Object contextSyncHandle = new Object();
        private final Object queueLock = new Object();

        private final Queue<PathResult> successfulPaths = new ArrayDeque<>();
        private final Queue<PathResult> failedPaths = new ArrayDeque<>();
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
                case FAILED -> handleAddition(path, failedPaths);
                default -> throw new IllegalStateException("path.state() must be either SUCCEEDED or FAILED");
            }
        }

        private void handleAddition(PathResult result, Queue<PathResult> target) {
            synchronized (queueLock) {
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

    }

    private PathResult processOperation(Context context, PathOperation operation) {
        try {
            boolean urgent = isUrgent(operation.searchArea(), context.blockProvider());
            Future<Boolean> syncResult = trySyncChunks(context, operation.searchArea(), urgent);

            /*
            for sync operations where the pathfinding operation would have very few chunks to work with, wait until
            synchronization is complete. this may happen when a PathOperation is first queued in an empty region
            where no chunks have been cached, or if it's in a region where there are cached chunks but they are very
            old

            if it isn't urgent, though, no need to wait on the sync
             */
            if(urgent) {
                syncResult.get();
            }

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
                    throw new InterruptedException();
                }

                //perform optimizations here in the future, such as somehow trying to merge PathOperations
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

    /**
     * Simple algorithm to quickly evaluate if the percentage of chunks for a given provider that are either outdated or
     * not present is high enough to warrant forcing a chunk sync
     */
    private boolean isUrgent(ChunkCoordinateProvider chunks, BlockCollisionProvider provider) {
        int presentAndFresh = 0;

        for(ChunkVectorAccess chunkVectorAccess : chunks) {
            CollisionChunkSnapshot chunk = provider.chunkAt(chunkVectorAccess.chunkX(), chunkVectorAccess.chunkZ());
            if(chunk != null && Bukkit.getCurrentTick() - chunk.captureTick() < MIN_CHUNK_SYNC_AGE) {
                presentAndFresh++;
            }
        }

        return (double)presentAndFresh / (double)chunks.chunkCount() < URGENT_SYNC_THRESHOLD;
    }

    /**
     * Attempts to perform a chunk sync operation for the given ChunkCoordinateProvider and Context. This operation
     * will be completed on the main server thread (necessary) and the returned result may be waited upon depending on
     * the desired behavior. A sync may or may not occur if force is set to false. If it occurs, the value of the
     * future (upon completion) will be set to true. If a sync doesn't occur for any reason, or an exception is thrown
     * during synchronization (in which case it may have partially completed) the value returned will be false.
     *
     * Synchronization can be forced by setting the force parameter to true. Note that the maximum number of sync tasks
     * queued at once will never be bypassed; if force is set to true, the future will wait indefinitely.
     */
    private Future<Boolean> trySyncChunks(Context context, ChunkCoordinateProvider coordinateProvider, boolean force) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        int currentTick = Bukkit.getCurrentTick();
        int age;
        boolean canUpdate;

        synchronized (context.contextSyncHandle) {
            age = currentTick - context.lastSyncTick;
            canUpdate = age >= MIN_CHUNK_SYNC_AGE;
        }

        /*
        defer to threads that have already scheduled synchronization on this context, up to a maximum of
        MAX_SCHEDULED_SYNC_TASKS. this is to avoid overloading the fragile BukkitScheduler (and the main thread), as
        well as avoiding potentially redundant sync attempts

        or, if we're forcing, schedule the sync no matter what. this is generally used in cases where the context
        really, really needs fresh chunks, such as for a new PathOperation or one that's going on in a really outdated
        area
        */
        if(force || (canUpdate && context.syncSemaphore.tryAcquire())) {
            if(force) {
                /*
                 * respect the hard limit of bukkit tasks, but we really need this operation to complete, so wait
                 * to acquire it
                 */
                context.syncSemaphore.acquireUninterruptibly();
            }

            CountDownLatch latch = new CountDownLatch(1);
            int taskId = Bukkit.getScheduler().runTask(ArenaApi.getInstance(), () -> {
                boolean noErr = true;
                try {
                    context.blockCollisionProvider.updateRegion(coordinateProvider);
                }
                catch (Exception exception) {
                    ArenaApi.warning("An exception occurred while synchronizing chunks:");
                    exception.printStackTrace();
                    noErr = false;
                }
                finally {
                    context.syncSemaphore.release();
                    result.complete(noErr);
                    latch.countDown();
                }
            }).getTaskId();

            //wait for the sync to complete
            try {
                if(force) {
                    latch.await(); //await forever if forced, to ensure pathing in urgent cases is not premature
                } else if(!latch.await(CHUNK_SYNC_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                    ArenaApi.warning("Chunk synchronizing took more than " + CHUNK_SYNC_TIMEOUT_MS + "ms! Is the server lagging?");
                    Bukkit.getScheduler().cancelTask(taskId);
                }
            } catch (InterruptedException interruptedException) {
                ArenaApi.warning("Interrupted while waiting for chunks to sync: ");
                interruptedException.printStackTrace();
                Bukkit.getScheduler().cancelTask(taskId);
            }

            synchronized (context.contextSyncHandle) {
                context.lastSyncTick = currentTick;
            }

            return result;
        }

        result.complete(false);
        return result;
    }

    /**
     * Queues a pathfinding operation onto the pathfinding thread. This method is thread-safe.
     * @param operation The operation to enqueue
     * @return A Future object representing the computation
     */
    @Override
    public @NotNull Future<PathResult> giveOperation(@NotNull PathOperation operation, @NotNull World world) {
        UUID worldID = world.getUID();
        Context context = contextMap.get(worldID);
        if(context == null) {
            context = new Context(new AsyncBlockCollisionProvider(world, MIN_CHUNK_SYNC_AGE));
            contextMap.put(worldID, context);
        }

        Context finalContext = context;
        return completionService.submit(() -> processOperation(finalContext, operation));
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @EventHandler
    private void onWorldUnload(WorldUnloadEvent event) {
        Context context = contextMap.remove(event.getWorld().getUID());
        if(context != null) {
            context.blockProvider().clearOwned();
        }
    }

    public static AsyncPathfinderEngine instance() {
        return INSTANCE;
    }

    /**
     * Re-register events on plugin reload or instantiation
     */
    public void registerEvents() {
        Bukkit.getServer().getPluginManager().registerEvents(this, ArenaApi.getInstance());
    }
}