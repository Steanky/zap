package io.github.zap.arenaapi.pathfind.engine;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.pathfind.chunk.ChunkBounds;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProviders;
import io.github.zap.arenaapi.pathfind.context.PathfinderContexts;
import io.github.zap.arenaapi.pathfind.context.SynchronizedPathfinderContext;
import io.github.zap.arenaapi.pathfind.operation.PathOperation;
import io.github.zap.arenaapi.pathfind.process.PathMergers;
import io.github.zap.vector.Vector2I;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

class AsyncSnapshotPathfinderEngine extends AsyncPathfinderEngineAbstract<SynchronizedPathfinderContext> implements Listener {
    private static final int MIN_CHUNK_SYNC_AGE = 40;
    private static final int PATH_CAPACITY = 32;
    private static final int MAX_SCHEDULED_SYNC_TASKS = 2;
    private static final double PERCENTAGE_STALE_REQUIRED_TO_FORCE = 0.9;
    private static final int CHUNK_SYNC_TIMEOUT_MS = 1000;

    AsyncSnapshotPathfinderEngine(@NotNull Plugin plugin) { //singleton
        super(new ConcurrentHashMap<>(), plugin);
    }

    @Override
    protected void preProcess(@NotNull SynchronizedPathfinderContext context, @NotNull PathOperation operation) {
        trySyncChunks(context, operation.searchArea(), isUrgent(operation.searchArea(), context.blockProvider()));
    }

    @Override
    protected @NotNull SynchronizedPathfinderContext makeContext(@NotNull BlockCollisionProvider provider) {
        return PathfinderContexts.synchronizedContext(provider, PathMergers.defaultMerger(), PATH_CAPACITY,
                MAX_SCHEDULED_SYNC_TASKS);
    }

    @Override
    protected @NotNull BlockCollisionProvider makeBlockCollisionProvider(@NotNull World world) {
        return BlockCollisionProviders.snapshotAsyncProvider(world, MAX_THREADS, MIN_CHUNK_SYNC_AGE);
    }

    /**
     * Simple algorithm to quickly evaluate if the percentage of chunks for a given provider that are either outdated or
     * not present is high enough to warrant forcing a chunk sync
     */
    private boolean isUrgent(ChunkBounds chunks, BlockCollisionProvider provider) {
        int stale = 0;

        for(Vector2I chunkVectorAccess : chunks) {
            CollisionChunkView chunk = provider.chunkAt(chunkVectorAccess.x(), chunkVectorAccess.z());

            if(chunk == null || (Bukkit.getCurrentTick() - chunk.captureTick()) > MIN_CHUNK_SYNC_AGE) {
                stale++;
            }
        }

        return (double)stale / (double)chunks.chunkCount() >= PERCENTAGE_STALE_REQUIRED_TO_FORCE;
    }

    private void trySyncChunks(SynchronizedPathfinderContext context, ChunkBounds coordinateProvider, boolean force) {
        int currentTick = Bukkit.getCurrentTick();

        /*
        defer to threads that have already scheduled synchronization on this context, up to a maximum of
        MAX_SCHEDULED_SYNC_TASKS. this is to avoid overloading the fragile BukkitScheduler (and the main thread), as
        well as avoiding potentially redundant sync attempts

        or, if we're forcing, schedule the sync no matter what. this is generally used in cases where the context
        really, really needs fresh chunks, such as for a new PathOperation or one that's going on in a really outdated
        area
        */
        if(force || (currentTick - context.lastSyncTick()) > MIN_CHUNK_SYNC_AGE && context.tryAcquirePermit()) {
            if(force) {
                /*
                 * respect the hard limit of bukkit tasks, but we really need this operation to complete, so wait
                 * to acquire it
                 */
                try {
                    context.acquirePermit();
                }
                catch (InterruptedException exception) {
                    plugin.getLogger().log(Level.WARNING, "Interrupted while attempting to force acquire " +
                            "synchronization semaphore", exception);
                    return;
                }
            }

            CountDownLatch latch = new CountDownLatch(1);
            int taskId = Bukkit.getScheduler().runTask(ArenaApi.getInstance(), () -> {
                try {
                    context.blockProvider().updateRegion(coordinateProvider);
                }
                catch (Exception exception) {
                    plugin.getLogger().log(Level.WARNING, "An exception occurred while synchronizing chunks", exception);
                }
                finally {
                    context.reportSync();
                    context.releasePermit();
                    latch.countDown();
                }
            }).getTaskId();

            //wait for the sync to complete
            try {
                if(force) {
                    latch.await(); //await forever if forced, to ensure pathing in urgent cases is not premature
                } else if(!latch.await(CHUNK_SYNC_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                    plugin.getLogger().log(Level.WARNING, "Chunk synchronizing took more than " + CHUNK_SYNC_TIMEOUT_MS + "ms! Is the server lagging?");
                    plugin.getLogger().log(Level.WARNING, "Cancelling sync.");
                    Bukkit.getScheduler().cancelTask(taskId); //don't bother to finish sync on a laggy server
                    latch.countDown(); //idk if necessary
                }
            } catch (InterruptedException exception) {
                plugin.getLogger().log(Level.WARNING, "Interrupted while waiting for chunks to sync", exception);
                Bukkit.getScheduler().cancelTask(taskId);
            }
        }
    }
}