package io.github.zap.nms.common.world;

import io.github.zap.vector.VectorAccess;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockCollisionSnapshot {
    @NotNull VectorAccess position();

    @NotNull BlockData data();

    @Nullable VoxelShapeWrapper collision();

    boolean overlaps(@NotNull BoundingBox relativeBounds);

    double height();

    static BlockCollisionSnapshot from(@NotNull VectorAccess chunkRelative, @NotNull BlockData data,
                                       @Nullable VoxelShapeWrapper shape) {
        return new BlockCollisionSnapshotImpl(chunkRelative, data, shape);
    }
}
