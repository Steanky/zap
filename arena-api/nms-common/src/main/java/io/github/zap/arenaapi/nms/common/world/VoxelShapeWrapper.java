package io.github.zap.arenaapi.nms.common.world;

import io.github.zap.vector.Bounds;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface VoxelShapeWrapper {
    double maxX();

    double minX();

    double maxY();

    double minY();

    double maxZ();

    double minZ();

    boolean isFull();

    boolean isEmpty();

    boolean isPartial();

    int size();

    @NotNull Bounds boundsAt(int index);

    boolean anyBoundsMatches(@NotNull BoxPredicate predicate);

    boolean collidesWith(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);

    default boolean collidesWith(@NotNull BoundingBox boundingBox) {
        return collidesWith(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(),
                boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
    }
}
