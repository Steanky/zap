package io.github.zap.nms.common.world;

import io.github.zap.vector.VectorAccess;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

class BlockSnapshotImpl implements BlockSnapshot {
    private final VectorAccess worldVector;
    private final BlockData data;
    private final VoxelShapeWrapper collision;

    BlockSnapshotImpl(@NotNull VectorAccess worldVector, @NotNull BlockData data,
                      @NotNull VoxelShapeWrapper collision) {
        this.worldVector = worldVector;
        this.data = data;
        this.collision = collision;
    }

    @Override
    public @NotNull VectorAccess position() {
        return worldVector;
    }

    @Override
    public @NotNull BlockData data() {
        return data;
    }

    @Override
    public @NotNull VoxelShapeWrapper collision() {
        return collision;
    }

    @Override
    public boolean overlaps(@NotNull BoundingBox worldBounds) {
        int x = worldVector.blockX();
        int y = worldVector.blockY();
        int z = worldVector.blockZ();

        return collision.collidesWith(worldBounds.getMinX() - x, worldBounds.getMinY() - y,
                worldBounds.getMinZ() - z, worldBounds.getMaxX() - x, worldBounds.getMaxY() - y,
                worldBounds.getMaxZ() - z);
    }
}
