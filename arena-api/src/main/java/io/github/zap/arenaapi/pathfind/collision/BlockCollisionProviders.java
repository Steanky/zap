package io.github.zap.arenaapi.pathfind.collision;

import io.github.zap.arenaapi.ArenaApi;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public final class BlockCollisionProviders {
    private static final long DEFAULT_TIMEOUT_INTERVAL = 2;
    private static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;

    public static @NotNull BlockCollisionProvider proxyAsyncProvider(@NotNull World world, int maxConcurrency) {
        return new ProxyBlockCollisionProvider(ArenaApi.getInstance().getNmsBridge().worldBridge(), world,
                maxConcurrency, DEFAULT_TIMEOUT_INTERVAL, DEFAULT_TIMEOUT_UNIT);
    }

    public static @NotNull BlockCollisionProvider proxyAsyncProvider(@NotNull World world, int maxConcurrency,
                                                                     long timeoutInterval, @NotNull TimeUnit timeoutUnit) {
        return new ProxyBlockCollisionProvider(ArenaApi.getInstance().getNmsBridge().worldBridge(), world,
                maxConcurrency, timeoutInterval, timeoutUnit);
    }

    public static @NotNull BlockCollisionProvider snapshotAsyncProvider(@NotNull World world, int maxConcurrency,
                                                                        int maxSnapshotAge) {
        return new SnapshotBlockCollisionProvider(ArenaApi.getInstance().getNmsBridge().worldBridge(), world,
                maxConcurrency, maxSnapshotAge);
    }
}
