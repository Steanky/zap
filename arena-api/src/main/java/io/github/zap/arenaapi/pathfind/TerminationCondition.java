package io.github.zap.arenaapi.pathfind;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

public interface TerminationCondition {
    /**
     * Simple termination condition: the path is complete when the agent reaches the node.
     */
    TerminationCondition REACHED_DESTINATION = (context, node, destination) -> node.equals(destination.node());

    boolean hasCompleted(@NotNull PathfinderContext context, @NotNull PathNode node, @NotNull PathDestination destination);

    static TerminationCondition whenWithin(double targetDistanceSquared) {
        Validate.isTrue(targetDistanceSquared > 0, "targetDistanceSquared must be greater than 0!");
        Validate.isTrue(Double.isFinite(targetDistanceSquared), "targetDistanceSquared must be finite!");
        return (context, node, destination) -> node.distanceSquaredTo(destination.node()) <= targetDistanceSquared;
    }

    static TerminationCondition whenSatisfies(@NotNull Predicate<PathNode> nodePredicate) {
        Objects.requireNonNull(nodePredicate, "nodePredicate cannot be null!");
        return (context, node, destination) -> nodePredicate.test(node);
    }

    static TerminationCondition both(@NotNull TerminationCondition first, @NotNull TerminationCondition second) {
        Objects.requireNonNull(first, "first cannot be null!");
        Objects.requireNonNull(first, "second cannot be null!");
        return (context, node, destination) -> first.hasCompleted(context, node, destination) && second.hasCompleted(context, node, destination);
    }

    static TerminationCondition either(@NotNull TerminationCondition first, @NotNull TerminationCondition second) {
        Objects.requireNonNull(first, "first cannot be null!");
        Objects.requireNonNull(first, "second cannot be null!");
        return (context, node, destination) -> first.hasCompleted(context, node, destination) || second.hasCompleted(context, node, destination);
    }
}
