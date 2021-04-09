package io.github.zap.nms.v1_16_R3.pathfind;

import io.github.zap.nms.common.pathfind.PathPointWrapper;
import net.minecraft.server.v1_16_R3.PathPoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PathPointWrapper_v1_16_R3 implements PathPointWrapper {
    private final PathPoint pathPoint;
    private final PathPointWrapper_v1_16_R3 parent;

    PathPointWrapper_v1_16_R3(@NotNull PathPoint pathPoint) {
        this.pathPoint = pathPoint;

        if(pathPoint.h != null) {
            this.parent = new PathPointWrapper_v1_16_R3(pathPoint.h);
        }
        else {
            this.parent = null;
        }
    }

    @Override
    public int getX() {
        return pathPoint.a;
    }

    @Override
    public int getY() {
        return pathPoint.b;
    }

    @Override
    public int getZ() {
        return pathPoint.c;
    }

    @Override
    public @Nullable PathPointWrapper parent() {
        return parent;
    }
}
