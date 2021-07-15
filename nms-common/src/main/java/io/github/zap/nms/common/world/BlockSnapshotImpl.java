package io.github.zap.nms.common.world;

import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

class BlockSnapshotImpl implements BlockSnapshot {
    private final int x;
    private final int y;
    private final int z;

    private final BlockData data;
    private final VoxelShapeWrapper collision;

    BlockSnapshotImpl(int x, int y, int z, @NotNull BlockData data,
                      @NotNull VoxelShapeWrapper collision) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.data = data;
        this.collision = collision;
    }

    @Override
    public int blockX() {
        return x;
    }

    @Override
    public int blockY() {
        return y;
    }

    @Override
    public int blockZ() {
        return z;
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
        return collision.collidesWith(worldBounds.getMinX() - x, worldBounds.getMinY() - y,
                worldBounds.getMinZ() - z, worldBounds.getMaxX() - x, worldBounds.getMaxY() - y,
                worldBounds.getMaxZ() - z);
    }
}
