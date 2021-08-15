package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

class ProxyPathfinderContextImpl extends PathfinderContextAbstract {
    ProxyPathfinderContextImpl(@NotNull BlockCollisionProvider blockCollisionProvider,
                               @NotNull PathMerger merger, int pathCapacity) {
        super(blockCollisionProvider, merger, pathCapacity);
    }
}
