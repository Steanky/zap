package io.github.zap.arenaapi.nms.common.world;

import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface VoxelShapeWrapper {
    double maxY();

    double minY();

    boolean isFull();

    boolean isEmpty();

    @NotNull List<BoundingBox> boundingBoxes();

    boolean anyBoundsMatches(@NotNull BoxPredicate predicate);

    boolean collidesWith(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
}
