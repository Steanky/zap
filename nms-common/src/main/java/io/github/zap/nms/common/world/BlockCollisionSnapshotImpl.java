package io.github.zap.nms.common.world;

import io.github.zap.vector.VectorAccess;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

class BlockCollisionSnapshotImpl implements BlockCollisionSnapshot {
    private final VectorAccess vector;
    private final BlockData data;
    private final VoxelShapeWrapper collision;

    BlockCollisionSnapshotImpl(@NotNull VectorAccess vector, @NotNull BlockData data, @NotNull VoxelShapeWrapper collision) {
        this.vector = vector;
        this.data = data;
        this.collision = collision;
    }

    @Override
    public @NotNull VectorAccess position() {
        return vector;
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
