package io.github.zap.arenaapi.pathfind.context;

public interface SynchronizedPathfinderContext extends PathfinderContext {
    int lastSyncTick();

    void acquirePermit() throws InterruptedException;

    boolean tryAcquirePermit();

    void releasePermit();

    void reportSync();
}
