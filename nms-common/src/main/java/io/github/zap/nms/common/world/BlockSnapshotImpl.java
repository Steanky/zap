package io.github.zap.nms.common.world;

import io.github.zap.vector.VectorAccess;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

class BlockSnapshotImpl implements BlockSnapshot {
    private final VectorAccess worldVector;
    private final BlockData data;
    private final VoxelShapeWrapper collision;
    private BoundingBox adjustBounds;

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
        return collision.collidesWith(worldBounds.getMinX() - worldVector.x(),
                worldBounds.getMinY() - worldVector.y(), worldBounds.getMinZ() - worldVector.z(),
                worldBounds.getMaxX() - worldVector.x(), worldBounds.getMaxY() - worldVector.y(),
                worldBounds.getMaxZ() - worldVector.z());
    }
}
