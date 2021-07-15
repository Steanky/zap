package io.github.zap.nms.common.world;

import io.github.zap.vector.ImmutableWorldVector;
import io.github.zap.vector.VectorAccess;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public interface BlockSnapshot {
    int blockX();

    int blockY();

    int blockZ();

    @NotNull BlockData data();

    @NotNull VoxelShapeWrapper collision();

    boolean overlaps(@NotNull BoundingBox worldBounds);

    static BlockSnapshot from(int x, int y, int z, @NotNull BlockData data,
                              @NotNull VoxelShapeWrapper shape) {
        return new BlockSnapshotImpl(x, y, z, data, shape);
    }
}
