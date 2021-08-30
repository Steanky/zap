package io.github.zap.arenaapi.pathfind.calculate;

import io.github.zap.arenaapi.pathfind.destination.PathDestination;
import io.github.zap.arenaapi.pathfind.path.PathNode;
import io.github.zap.arenaapi.pathfind.context.PathfinderContext;
import org.jetbrains.annotations.NotNull;

/**
 * Responsible for calculating the heuristic value, h(x), for use with an A* based pathfinding implementation. A
 * "typical" implementation returns the distance between the input node and the destination.
 */
@FunctionalInterface
public interface HeuristicCalculator {
    /**
     * Computes the heuristic, h(x), for a given {@link PathNode} and {@link PathDestination} in a
     * {@link PathfinderContext}. Most implementations will just return the distance between the PathNode and the
     * PathDestination.
     * @param context The PathfinderContext we're currently in
     * @param from The PathNode to calculate the heuristic for
     * @param destination The destination we're travelling to
     * @return The heuristic value. This may be any double value.
     */
    double compute(@NotNull PathfinderContext context, @NotNull PathNode from, @NotNull PathDestination destination);
}
