package io.github.zap.arenaapi.pathfind.collision;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public final class BlockCollisionProviders {
    public static @NotNull BlockCollisionProvider proxyAsyncProvider(@NotNull World world, int maxConcurrency) {
        return new ProxyBlockCollisionProvider(world, maxConcurrency);
    }

    public static @NotNull BlockCollisionProvider snapshotAsyncProvider(@NotNull World world, int maxConcurrency,
                                                                        int maxSnapshotAge) {
        return new SnapshotBlockCollisionProvider(world, maxConcurrency, maxSnapshotAge);
    }
}
