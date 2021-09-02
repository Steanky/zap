package io.github.zap.arenaapi.nms.common.world;

import io.github.zap.vector.Bounds;
import io.github.zap.vector.Direction;
import io.github.zap.vector.Vector3D;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;


public interface VoxelShapeWrapper {
    boolean isFull();

    boolean isEmpty();

    boolean isPartial();

    int size();

    @NotNull Bounds boundsAt(int index);

    boolean anyBoundsMatches(@NotNull BoxPredicate predicate);

    boolean collidesWith(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);

    @NotNull Vector3D positionAtSide(@NotNull Direction direction);

    default boolean collidesWith(@NotNull BoundingBox boundingBox) {
        return collidesWith(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(),
                boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
    }
}
