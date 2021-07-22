package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.Vectors;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface HeuristicCalculator {
    HeuristicCalculator DISTANCE_ONLY = (context, current, destination) -> Vectors.distance(current, destination);

    double compute(@NotNull PathfinderContext context, @NotNull PathNode from, @NotNull PathDestination destination);
}
