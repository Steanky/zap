package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.ImmutableWorldVector;
import io.github.zap.arenaapi.vector.VectorAccess;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

public interface SuccessCondition {
    /**
     * Simple termination condition: the path is complete when the agent occupies the same block as the destination.
     */
    SuccessCondition WITHIN_BLOCK = (context, node, destination) -> {
        VectorAccess position = destination.position();
        return node.blockX() == position.blockX() && node.blockY() == position.blockY() && node.blockZ() == position.blockZ();
    };

    boolean hasCompleted(@NotNull PathfinderContext context, @NotNull PathNode node, @NotNull PathDestination destination);

    static @NotNull SuccessCondition whenWithin(double distanceSquared) {
        Validate.isTrue(distanceSquared > 0, "distanceSquared must be greater than 0!");
        Validate.isTrue(Double.isFinite(distanceSquared), "distanceSquared must be finite!");
        return (context, node, destination) -> destination.position().distanceSquared(node) <= distanceSquared;
    }

    static @NotNull SuccessCondition when(@NotNull Predicate<PathNode> nodePredicate) {
        Objects.requireNonNull(nodePredicate, "nodePredicate cannot be null!");
        return (context, node, destination) -> nodePredicate.test(node);
    }

    static @NotNull SuccessCondition whenBoth(@NotNull SuccessCondition first, @NotNull SuccessCondition second) {
        Objects.requireNonNull(first, "first cannot be null!");
        Objects.requireNonNull(first, "second cannot be null!");
        return (context, node, destination) -> first.hasCompleted(context, node, destination) && second.hasCompleted(context, node, destination);
    }

    static @NotNull SuccessCondition whenEither(@NotNull SuccessCondition first, @NotNull SuccessCondition second) {
        Objects.requireNonNull(first, "first cannot be null!");
        Objects.requireNonNull(first, "second cannot be null!");
        return (context, node, destination) -> first.hasCompleted(context, node, destination) || second.hasCompleted(context, node, destination);
    }
}
