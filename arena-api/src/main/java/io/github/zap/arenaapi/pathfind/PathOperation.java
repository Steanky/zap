package io.github.zap.arenaapi.pathfind;

import org.apache.commons.lang3.Validate;
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

    @NotNull Set<? extends PathDestination> getDestinations();

    @NotNull Set<PathNode> visitedNodes();

    @NotNull PathAgent getAgent();

    static PathOperation forAgent(@NotNull PathAgent agent, @NotNull Set<? extends PathDestination> destinations,
                                  @NotNull CostCalculator calculator, @NotNull TerminationCondition terminationCondition,
                                  @NotNull NodeProvider provider, @NotNull DestinationSelector destinationSelector) {
        Objects.requireNonNull(agent, "agent cannot be null!");
        Objects.requireNonNull(destinations, "destinations cannot be null!");
        Validate.isTrue(!destinations.isEmpty(), "destinations cannot be empty!");
        Objects.requireNonNull(calculator, "calculator cannot be null!");
        Objects.requireNonNull(terminationCondition,"terminationCondition cannot be null!");
        Objects.requireNonNull(provider, "provider cannot be null!");
        Objects.requireNonNull(destinationSelector, "destinationSelector cannot be null!");

        return new PathOperationImpl(agent, destinations, calculator, terminationCondition, provider, destinationSelector);
    }
}