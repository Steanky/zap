package io.github.zap.arenaapi.pathfind.context;

import io.github.zap.arenaapi.pathfind.process.PathMerger;
import io.github.zap.arenaapi.pathfind.operation.PathOperation;
import io.github.zap.arenaapi.pathfind.path.PathResult;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.Semaphore;

class SynchronizedPathfinderContextImpl extends PathfinderContextAbstract implements SynchronizedPathfinderContext {
    private final Semaphore semaphore;
    private int lastSyncTick;

    SynchronizedPathfinderContextImpl(@NotNull BlockCollisionProvider blockCollisionProvider, @NotNull PathMerger merger,
                                      int pathCapacity, int initialPermits) {
        super(blockCollisionProvider, merger, pathCapacity);
        this.semaphore = new Semaphore(initialPermits);
        lastSyncTick = Bukkit.getCurrentTick();
    }


    private void handleAddition(PathResult result, Queue<PathResult> target) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (target) {
            int oldCount = target.size();
            int newCount = oldCount + 1;

            target.add(result);

            if(newCount == pathCapacity) {
                target.poll();
            }
        }
    }

    @Override
    public void recordPath(@NotNull PathResult path) {
        PathOperation.State state = path.state();

        if (state == PathOperation.State.SUCCEEDED) {
            handleAddition(path, successfulPaths);
        }
    }

    @Override
    public int lastSyncTick() {
        return lastSyncTick;
    }

    @Override
    public void acquirePermit() throws InterruptedException {
        semaphore.acquire();
    }

    @Override
    public boolean tryAcquirePermit() {
        return semaphore.tryAcquire();
    }

    @Override
    public void releasePermit() {
        semaphore.release();
    }

    @Override
    public void reportSync() {
        lastSyncTick = Bukkit.getCurrentTick();
    }
}
