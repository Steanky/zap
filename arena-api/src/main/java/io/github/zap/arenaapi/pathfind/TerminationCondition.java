package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public interface TerminationCondition {
    boolean hasCompleted(@NotNull PathfinderContext context, @NotNull PathNode node, @NotNull PathDestination destination);

    static TerminationCondition whenWithin(int targetDistanceSquared) {
        return (context, node, destination) -> node.distanceSquaredTo(destination.targetNode()) <= targetDistanceSquared;
    }

    static TerminationCondition whenSatisfies(@NotNull Predicate<PathNode> nodePredicate) {
        return (context, node, destination) -> nodePredicate.test(node);
    }
}
