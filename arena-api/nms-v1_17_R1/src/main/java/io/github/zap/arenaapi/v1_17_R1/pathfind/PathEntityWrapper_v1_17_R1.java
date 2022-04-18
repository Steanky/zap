package io.github.zap.arenaapi.v1_17_R1.pathfind;

import io.github.zap.arenaapi.nms.common.pathfind.PathEntityWrapper;
import net.minecraft.world.level.pathfinder.Path;

public record PathEntityWrapper_v1_17_R1(Path pathEntity) implements PathEntityWrapper {
    @Override
    public int pathLength() {
        return pathEntity.getNodeCount();
    }
}
