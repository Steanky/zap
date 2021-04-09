package io.github.zap.nms.common.world;

import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public interface VoxelShapeWrapper {
    boolean containsPoint(double x, double y, double z);

    boolean overlaps(@NotNull BoundingBox boundingBox);
}
