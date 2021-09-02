package io.github.zap.arenaapi.nms.common.world;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * Proxies NMS methods relating to worlds.
 */
public interface WorldBridge {
    /**
     * Returns the default world name; i.e. the one defined in server.properties.
     * @return The default world name
     */
    @NotNull String getDefaultWorldName();

    @NotNull CollisionChunkView proxyView(@NotNull Chunk chunk, int expectedConcurrency, long timeoutInterval,
                                          @NotNull TimeUnit timeoutUnit);

    @NotNull CollisionChunkView snapshotView(@NotNull Chunk chunk);

    boolean blockHasCollision(@NotNull Block block);

    @NotNull BlockCollisionView collisionFor(@NotNull Block block);

    @Nullable Chunk getChunkIfLoadedImmediately(@NotNull World world, int x, int z);
}
