package io.github.zap.arenaapi.v1_17_R1.pathfind;

import io.github.zap.arenaapi.nms.common.pathfind.PathPointWrapper;
import net.minecraft.world.level.pathfinder.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PathPointWrapper_v1_17_R1 implements PathPointWrapper {
    private final Node pathPoint;
    private PathPointWrapper parent;

    public PathPointWrapper_v1_17_R1(@NotNull Node pathPoint) {
        this.pathPoint = pathPoint;
        pathPoint.closed = true;
    }

    @Override
    public int x() {
        return pathPoint.x;
    }

    @Override
    public int y() {
        return pathPoint.y;
    }

    @Override
    public int z() {
        return pathPoint.z;
    }

    @Override
    public @Nullable PathPointWrapper parent() {
        return parent;
    }

    @Override
    public void setParent(@Nullable PathPointWrapper parent) {
        this.parent = parent;
        pathPoint.cameFrom = parent == null ? null : ((PathPointWrapper_v1_17_R1)parent).pathPoint;
    }

    public @NotNull Node pathPoint() {
        return pathPoint;
    }
}
