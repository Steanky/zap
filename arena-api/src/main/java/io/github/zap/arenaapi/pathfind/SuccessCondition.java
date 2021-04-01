package io.github.zap.arenaapi.pathfind;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

public interface SuccessCondition {
    /**
     * Simple termination condition: the path is complete when the agent reaches the node.
     */
    SuccessCondition REACHED_DESTINATION = (context, node, destination) -> node.equals(destination.node());

    boolean hasCompleted(@NotNull PathfinderContext context, @NotNull PathNode node, @NotNull PathDestination destination);

    static SuccessCondition whenWithin(double targetDistanceSquared) {
        Validate.isTrue(targetDistanceSquared > 0, "targetDistanceSquared must be greater than 0!");
        Validate.isTrue(Double.isFinite(targetDistanceSquared), "targetDistanceSquared must be finite!");
        return (context, node, destination) -> node.distanceSquaredTo(destination.node()) <= targetDistanceSquared;
    }

    static SuccessCondition whenSatisfies(@NotNull Predicate<PathNode> nodePredicate) {
        Objects.requireNonNull(nodePredicate, "nodePredicate cannot be null!");
        return (context, node, destination) -> nodePredicate.test(node);
    }

    static SuccessCondition both(@NotNull SuccessCondition first, @NotNull SuccessCondition second) {
        Objects.requireNonNull(first, "first cannot be null!");
        Objects.requireNonNull(first, "second cannot be null!");
        return (context, node, destination) -> first.hasCompleted(context, node, destination) && second.hasCompleted(context, node, destination);
    }

    static SuccessCondition either(@NotNull SuccessCondition first, @NotNull SuccessCondition second) {
        Objects.requireNonNull(first, "first cannot be null!");
        Objects.requireNonNull(first, "second cannot be null!");
        return (context, node, destination) -> first.hasCompleted(context, node, destination) || second.hasCompleted(context, node, destination);
    }
}
