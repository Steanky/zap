package io.github.zap.arenaapi.pathfind.context;

import io.github.zap.arenaapi.pathfind.process.PathMerger;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import org.jetbrains.annotations.NotNull;

class ProxyPathfinderContextImpl extends PathfinderContextAbstract {
    ProxyPathfinderContextImpl(@NotNull BlockCollisionProvider blockCollisionProvider,
                               @NotNull PathMerger merger, int pathCapacity) {
        super(blockCollisionProvider, merger, pathCapacity);
    }
}
