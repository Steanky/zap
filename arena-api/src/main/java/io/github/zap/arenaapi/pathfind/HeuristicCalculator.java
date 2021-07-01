package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

public interface HeuristicCalculator {
    HeuristicCalculator DISTANCE_ONLY = (context, current, destination) -> current.distance(destination.position());

    double compute(@NotNull PathfinderContext context, @NotNull PathNode from, @NotNull PathDestination destination);
}
