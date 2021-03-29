package io.github.zap.arenaapi.pathfind;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public interface PathOperation {
    enum State {
        INCOMPLETE,
        SUCCEEDED,
        FAILED
    }

    boolean step(@NotNull PathfinderContext context);

    @NotNull PathOperation.State getState();

    @NotNull PathResult getResult();

    int desiredIterations();

    boolean shouldRemove();

    @NotNull Set<PathDestination> getDestinations();

    @NotNull Set<PathNode> visitedNodes();

    @NotNull PathAgent getAgent();

    static PathOperation forAgent(@NotNull PathAgent agent, @NotNull PathDestination destination) {
        Objects.requireNonNull(agent, "agent cannot be null!");
        Objects.requireNonNull(destination, "destination cannot be null!");

        return new PathOperationImpl(agent, ImmutableSet.of(destination), CostCalculator.DISTANCE_ONLY,
                TerminationCondition.REACHED_DESTINATION, NodeProvider.DEBUG, DestinationSelector.CLOSEST);
    }
}