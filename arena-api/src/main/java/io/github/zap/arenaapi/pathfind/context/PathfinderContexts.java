package io.github.zap.arenaapi.pathfind.context;

import io.github.zap.arenaapi.pathfind.process.PathMerger;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import org.jetbrains.annotations.NotNull;

public class PathfinderContexts {
    public static @NotNull SynchronizedPathfinderContext synchronizedContext(@NotNull BlockCollisionProvider provider,
                                                                             @NotNull PathMerger merger, int pathCapacity,
                                                                             int initialPermits) {
        return new SynchronizedPathfinderContextImpl(provider, merger, pathCapacity, initialPermits);
    }

    public static @NotNull PathfinderContext proxyContext(@NotNull BlockCollisionProvider provider,
                                                          @NotNull PathMerger merger, int pathCapacity) {
        return new ProxyPathfinderContextImpl(provider, merger, pathCapacity);
    }
}
