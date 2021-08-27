package io.github.zap.arenaapi.pathfind.calculate;

import io.github.zap.arenaapi.pathfind.path.PathNode;
import io.github.zap.arenaapi.pathfind.context.PathfinderContext;
import io.github.zap.arenaapi.pathfind.destination.PathDestination;
import io.github.zap.vector.Vectors;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface SuccessCondition {
    /**
     * Simple termination condition: the path is complete when the agent occupies the same block as the destination.
     */
    SuccessCondition SAME_BLOCK = (context, node, destination) -> Vectors.equals(node, destination);

    boolean hasCompleted(@NotNull PathfinderContext context, @NotNull PathNode node, @NotNull PathDestination destination);
}
