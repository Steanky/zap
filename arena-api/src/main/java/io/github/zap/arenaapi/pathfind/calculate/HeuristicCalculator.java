package io.github.zap.arenaapi.pathfind.calculate;

import io.github.zap.arenaapi.pathfind.destination.PathDestination;
import io.github.zap.arenaapi.pathfind.path.PathNode;
import io.github.zap.arenaapi.pathfind.context.PathfinderContext;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface HeuristicCalculator {
    double compute(@NotNull PathfinderContext context, @NotNull PathNode from, @NotNull PathDestination destination);
}
