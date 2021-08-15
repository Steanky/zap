package io.github.zap.arenaapi.pathfind;

public interface SynchronizedPathfinderContext extends PathfinderContext {
    int lastSyncTick();

    void acquirePermit() throws InterruptedException;

    boolean tryAcquirePermit();

    void releasePermit();

    void reportSync();
}
