package io.github.zap.arenaapi.pathfind.collision;

import io.github.zap.arenaapi.ArenaApi;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public final class BlockCollisionProviders {
    public static @NotNull BlockCollisionProvider proxyAsyncProvider(@NotNull World world, int maxConcurrency) {
        return new ProxyBlockCollisionProvider(ArenaApi.getInstance().getNmsBridge().worldBridge(), world, maxConcurrency);
    }

    public static @NotNull BlockCollisionProvider snapshotAsyncProvider(@NotNull World world, int maxConcurrency,
                                                                        int maxSnapshotAge) {
        return new SnapshotBlockCollisionProvider(ArenaApi.getInstance().getNmsBridge().worldBridge(), world,
                maxConcurrency, maxSnapshotAge);
    }
}
