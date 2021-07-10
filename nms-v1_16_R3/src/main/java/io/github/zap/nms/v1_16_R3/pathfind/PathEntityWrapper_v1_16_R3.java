package io.github.zap.nms.v1_16_R3.pathfind;

import io.github.zap.nms.common.pathfind.PathEntityWrapper;
import net.minecraft.server.v1_16_R3.PathEntity;
import org.jetbrains.annotations.NotNull;

public class PathEntityWrapper_v1_16_R3 implements PathEntityWrapper {
    private final PathEntity pathEntity;

    public PathEntityWrapper_v1_16_R3(@NotNull PathEntity pathEntity) {
        this.pathEntity = pathEntity;
    }

    public @NotNull PathEntity pathEntity() {
        return pathEntity;
    }

    @Override
    public int pathLength() {
        return pathEntity.e();
    }
}
