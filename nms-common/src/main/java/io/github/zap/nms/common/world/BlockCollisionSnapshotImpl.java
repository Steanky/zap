package io.github.zap.nms.common.world;

import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class BlockCollisionSnapshotImpl implements BlockCollisionSnapshot {
    private final BlockData data;
    private final VoxelShapeWrapper collision;

    BlockCollisionSnapshotImpl(@NotNull BlockData data, @NotNull VoxelShapeWrapper collision) {
        this.data = data;
        this.collision = collision;
    }

    @Override
    public @NotNull BlockData data() {
        return data;
    }

    @Override
    public @NotNull VoxelShapeWrapper collision() {
        return collision;
    }
}
