package io.github.zap.nms.v1_16_R3.pathfind;

import io.github.zap.nms.common.pathfind.PathPointWrapper;
import io.github.zap.vector.VectorAccess;
import net.minecraft.server.v1_16_R3.PathPoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PathPointWrapper_v1_16_R3 implements PathPointWrapper {
    private final PathPoint pathPoint;
    private PathPointWrapper parent;

    public PathPointWrapper_v1_16_R3(@NotNull PathPoint pathPoint) {
        this.pathPoint = pathPoint;
    }

    @Override
    public int getX() {
        return pathPoint.getX();
    }

    @Override
    public int getY() {
        return pathPoint.getY();
    }

    @Override
    public int getZ() {
        return pathPoint.getZ();
    }

    @Override
    public @Nullable PathPointWrapper parent() {
        return parent;
    }

    @Override
    public void setParent(@Nullable PathPointWrapper parent) {
        this.parent = parent;
        pathPoint.h = parent == null ? null : ((PathPointWrapper_v1_16_R3)parent).pathPoint;
    }

    public @NotNull PathPoint pathPoint() {
        return pathPoint;
    }
}
