package io.github.zap.arenaapi.nms.v1_16_R3.pathfind;

import io.github.zap.arenaapi.nms.common.pathfind.PathEntityWrapper;
import net.minecraft.server.v1_16_R3.PathEntity;

public record PathEntityWrapper_v1_16_R3(PathEntity pathEntity) implements PathEntityWrapper {
    @Override
    public int pathLength() {
        return pathEntity.e();
    }
}
