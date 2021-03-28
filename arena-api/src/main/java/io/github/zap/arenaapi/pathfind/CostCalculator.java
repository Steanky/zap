package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.util.VectorUtils;
import org.jetbrains.annotations.NotNull;

public interface CostCalculator {
    CostCalculator BASIC = (context, from, to, destination) -> {
        PathNode node = destination.targetNode();
        return VectorUtils.distanceSquared(from.x, from.y, from.z, to.x, to.y, to.z) +
                VectorUtils.distanceSquared(to.x, to.y, to.z, node.x, node.y, node.z);
    };

    int computeCost(@NotNull PathfinderContext context, @NotNull PathNode from, @NotNull PathNode to, @NotNull PathDestination destination);
}
