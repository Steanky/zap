package io.github.zap.arenaapi.pathfind.calculate;

import io.github.zap.arenaapi.pathfind.path.PathNode;
import io.github.zap.arenaapi.pathfind.context.PathfinderContext;
import io.github.zap.arenaapi.pathfind.destination.PathDestination;
import io.github.zap.vector.Vectors;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface SuccessCondition {
    boolean hasCompleted(@NotNull PathfinderContext context, @NotNull PathNode node, @NotNull PathDestination destination);
}
