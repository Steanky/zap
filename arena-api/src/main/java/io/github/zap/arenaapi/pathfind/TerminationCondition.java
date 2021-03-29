package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public interface TerminationCondition {
    /**
     * Simple termination condition: the path is complete when the agent reaches the node.
     */
    TerminationCondition SIMPLE = (context, node, destination) -> node.equals(destination.node());

    boolean hasCompleted(@NotNull PathfinderContext context, @NotNull PathNode node, @NotNull PathDestination destination);

    static TerminationCondition whenWithin(int targetDistanceSquared) {
        return (context, node, destination) -> node.distanceSquaredTo(destination.node()) <= targetDistanceSquared;
    }

    static TerminationCondition whenSatisfies(@NotNull Predicate<PathNode> nodePredicate) {
        return (context, node, destination) -> nodePredicate.test(node);
    }
}
