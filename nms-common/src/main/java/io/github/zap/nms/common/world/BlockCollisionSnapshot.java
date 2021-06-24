package io.github.zap.nms.common.world;

import io.github.zap.vector.VectorAccess;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

public interface BlockCollisionSnapshot {
    @NotNull VectorAccess position();

    @NotNull BlockData data();

    @NotNull VoxelShapeWrapper collision();

    static BlockCollisionSnapshot from(@NotNull VectorAccess vector, @NotNull BlockData data,
                                       @NotNull VoxelShapeWrapper shape) {
        return new BlockCollisionSnapshotImpl(vector, data, shape);
    }
}
