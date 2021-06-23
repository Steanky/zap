package io.github.zap.nms.common.world;

import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockCollisionSnapshot {
    @NotNull BlockData data();

    @NotNull VoxelShapeWrapper collision();

    static BlockCollisionSnapshot from(@NotNull BlockData data, @NotNull VoxelShapeWrapper shape) {
        return new BlockCollisionSnapshotImpl(data, shape);
    }
}
