package io.github.zap.arenaapi.nms.common.world;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Proxies NMS methods relating to worlds.
 */
public interface WorldBridge {
    /**
     * Returns the default world name; i.e. the one defined in server.properties.
     * @return The default world name
     */
    @NotNull String getDefaultWorldName();

    @NotNull CollisionChunkSnapshot takeSnapshot(@NotNull Chunk chunk);

    boolean blockHasCollision(@NotNull Block block);

    @NotNull VoxelShapeWrapper collisionShapeFor(@NotNull Block block);
}
