package io.github.zap.nms.world;

import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public interface WrappedVoxelShape {
    boolean containsPoint(double x, double y, double z);

    boolean overlaps(@NotNull BoundingBox boundingBox);
}
