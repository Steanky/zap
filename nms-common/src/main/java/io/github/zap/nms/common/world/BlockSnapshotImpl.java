package io.github.zap.nms.common.world;

import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

record BlockSnapshotImpl(int x, int y, int z, BlockData data, VoxelShapeWrapper collision) implements BlockSnapshot {
    @Override
    public boolean overlaps(@NotNull BoundingBox worldBounds) {
        return collision.collidesWith(worldBounds.getMinX() - x, worldBounds.getMinY() - y,
                worldBounds.getMinZ() - z, worldBounds.getMaxX() - x, worldBounds.getMaxY() - y,
                worldBounds.getMaxZ() - z);
    }
}
