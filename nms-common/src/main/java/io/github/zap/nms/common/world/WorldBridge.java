package io.github.zap.nms.common.world;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
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

    @NotNull VoxelShapeWrapper collisionShapeAt(@NotNull World world, @NotNull BlockData data, int x, int y, int z);

    @NotNull VoxelShapeWrapper collisionShape(@NotNull World world, @NotNull BlockData data);

    @NotNull SimpleChunkSnapshot takeSnapshot(@NotNull Chunk chunk);

    boolean isValidChunkCoordinate(int x, int y, int z);
}
