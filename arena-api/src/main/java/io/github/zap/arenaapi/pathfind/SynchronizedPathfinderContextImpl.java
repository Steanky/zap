package io.github.zap.arenaapi.pathfind;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

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
