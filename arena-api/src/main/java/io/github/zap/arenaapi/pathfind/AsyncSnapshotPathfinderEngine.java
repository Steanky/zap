package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.vector.Vector2I;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AsyncSnapshotPathfinderEngine extends AsyncPathfinderEngineAbstract<SynchronizedPathfinderContext> implements Listener {
    private static final AsyncSnapshotPathfinderEngine INSTANCE = new AsyncSnapshotPathfinderEngine();

    private static final int MIN_CHUNK_SYNC_AGE = 40;
    private static final int PATH_CAPACITY = 32;
    private static final int MAX_SCHEDULED_SYNC_TASKS = 2;
    private static final double PERCENTAGE_STALE_REQUIRED_TO_FORCE = 0.9;
    private static final int CHUNK_SYNC_TIMEOUT_MS = 1000;

    @Override
    protected void preProcess(@NotNull SynchronizedPathfinderContext context, @NotNull PathOperation operation) {
        trySyncChunks(context, operation.searchArea(), isUrgent(operation.searchArea(), context.blockProvider()));
    }

    @Override
    protected @NotNull SynchronizedPathfinderContext makeContext(@NotNull BlockCollisionProvider provider) {
        return new SynchronizedPathfinderContextImpl(provider, new PathMergerImpl(), MAX_SCHEDULED_SYNC_TASKS, PATH_CAPACITY);
    }

    @Override
    protected @NotNull BlockCollisionProvider getBlockCollisionProvider(@NotNull World world) {
        return new SnapshotBlockCollisionProvider(world, MIN_CHUNK_SYNC_AGE);
    }

    private AsyncSnapshotPathfinderEngine() { //singleton
        super(new ConcurrentHashMap<>());
        Bukkit.getServer().getPluginManager().registerEvents(this, ArenaApi.getInstance());
    }

    /**
     * Simple algorithm to quickly evaluate if the percentage of chunks for a given provider that are either outdated or
     * not present is high enough to warrant forcing a chunk sync
     */
    private boolean isUrgent(ChunkCoordinateProvider chunks, BlockCollisionProvider provider) {
        int stale = 0;

        for(Vector2I chunkVectorAccess : chunks) {
            CollisionChunkView chunk = provider.chunkAt(chunkVectorAccess.x(), chunkVectorAccess.z());

            if(chunk == null || (Bukkit.getCurrentTick() - chunk.captureTick()) > MIN_CHUNK_SYNC_AGE) {
                stale++;
            }
        }

        return (double)stale / (double)chunks.chunkCount() >= PERCENTAGE_STALE_REQUIRED_TO_FORCE;
    }

    private void trySyncChunks(SynchronizedPathfinderContext context, ChunkCoordinateProvider coordinateProvider, boolean force) {
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
                catch (InterruptedException e) {
                    ArenaApi.warning("Interrupted while attempting to force acquire synchronization semaphore.");
                    return;
                }
            }

            CountDownLatch latch = new CountDownLatch(1);
            int taskId = Bukkit.getScheduler().runTask(ArenaApi.getInstance(), () -> {
                try {
                    context.blockProvider().updateRegion(coordinateProvider);
                }
                catch (Exception exception) {
                    ArenaApi.warning("An exception occurred while synchronizing chunks:");
                    exception.printStackTrace();
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

    public static AsyncSnapshotPathfinderEngine getInstance() {
        return INSTANCE;
    }

    @EventHandler
    private void onWorldUnload(WorldUnloadEvent event) {
        PathfinderContext context = contexts.get(event.getWorld().getUID());

        if(context != null) {
            context.blockProvider().clearForWorld();
        }
    }
}