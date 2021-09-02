package io.github.zap.arenaapi.nms.common.world;

import io.github.zap.vector.Direction;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

record BlockCollisionViewImpl(int x, int y, int z, BlockData data, VoxelShapeWrapper collision) implements BlockCollisionView {
    @Override
    public boolean overlaps(@NotNull BoundingBox worldBounds) {
        return collision.collidesWith(worldBounds.getMinX() - x, worldBounds.getMinY() - y,
                worldBounds.getMinZ() - z, worldBounds.getMaxX() - x, worldBounds.getMaxY() - y,
                worldBounds.getMaxZ() - z);
    }

    @Override
    public double exactY() {
        double collisionY = collision.positionAtSide(Direction.UP).y();
        if(Double.isFinite(collisionY)) {
            return y + collisionY;
        }

        return y;
    }
}
