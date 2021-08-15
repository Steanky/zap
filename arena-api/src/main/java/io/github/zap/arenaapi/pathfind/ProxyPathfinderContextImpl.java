package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

class ProxyPathfinderContextImpl extends PathfinderContextAbstract {
    ProxyPathfinderContextImpl(@NotNull BlockCollisionProvider blockCollisionProvider,
                               @NotNull PathMerger merger, int pathCapacity) {
        super(blockCollisionProvider, merger, pathCapacity);
    }

    @Override
    public void recordPath(@NotNull PathResult result) {
        if(result.state() == PathOperation.State.SUCCEEDED) {
            successfulPaths.add(result);
        }
    }
}
