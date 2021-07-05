package io.github.zap.nms.common.world;

import io.github.zap.vector.ImmutableWorldVector;
import io.github.zap.vector.VectorAccess;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public interface BlockSnapshot {
    @NotNull VectorAccess position();

    @NotNull BlockData data();

    @NotNull VoxelShapeWrapper collision();

    boolean overlaps(@NotNull BoundingBox worldBounds);

    static BlockSnapshot from(@NotNull ImmutableWorldVector worldRelative, @NotNull BlockData data,
                              @NotNull VoxelShapeWrapper shape) {
        return new BlockSnapshotImpl(worldRelative, data, shape);
    }
}
