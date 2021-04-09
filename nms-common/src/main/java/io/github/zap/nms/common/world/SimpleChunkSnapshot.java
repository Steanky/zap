package io.github.zap.nms.common.world;

import org.bukkit.ChunkSnapshot;
import org.jetbrains.annotations.Nullable;

public interface SimpleChunkSnapshot extends ChunkSnapshot {
    /**
     * Returns a WrappedVoxelShape object for the block at the specified chunk-relative coordinates. This function
     * will return null if the block's collision box is a perfect 1x1x1 cube.
     * @param chunkX The chunk-relative x value
     * @param chunkY The chunk-relative y value
     * @param chunkZ The chunk-relative z value
     * @return A WrappedVoxelShape object, or null if the block is not partial
     */
    @Nullable VoxelShapeWrapper collisionFor(int chunkX, int chunkY, int chunkZ);
}
