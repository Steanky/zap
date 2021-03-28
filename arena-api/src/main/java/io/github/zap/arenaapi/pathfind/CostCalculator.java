package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.util.VectorUtils;
import org.jetbrains.annotations.NotNull;

public interface CostCalculator {
    /**
     * Basic A* node cost calculator implementation: only takes into account the distance between nodes and nothing
     * else.
     */
    CostCalculator SIMPLE = (context, from, to, destination) -> {
        PathNode target = destination.targetNode();

        return new Cost(from.cost.nodeCost + VectorUtils.distanceSquared(from.x, from.y, from.z, to.x, to.y, to.z),
                VectorUtils.distanceSquared(to.x, to.y, to.z, target.x, target.y, target.z));
    };

    Cost computeCost(@NotNull PathfinderContext context, @NotNull PathNode from, @NotNull PathNode to, @NotNull PathDestination destination);
}
