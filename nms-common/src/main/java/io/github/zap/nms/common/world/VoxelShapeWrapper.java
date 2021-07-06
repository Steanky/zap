package io.github.zap.nms.common.world;

import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface VoxelShapeWrapper {
    void forEachBox(@NotNull BoxConsumer consumer);

    double maxY();

    double minY();

    boolean isFull();

    boolean isEmpty();

    @NotNull List<BoundingBox> boundingBoxes();

    boolean collidesWith(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
}
