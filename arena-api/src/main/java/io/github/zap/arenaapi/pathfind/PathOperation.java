package io.github.zap.arenaapi.pathfind;

import org.apache.commons.lang3.Validate;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public interface PathOperation {
    enum State {
        NOT_STARTED,
        STARTED,
        SUCCEEDED,
        FAILED;

        public boolean hasEnded() {
            return this == SUCCEEDED || this == FAILED;
        }

        public boolean hasStarted() {
            return this == STARTED;
        }
    }

    void init();

    boolean allowMerge(@NotNull PathOperation other);

    boolean step(@NotNull PathfinderContext context);

    @NotNull PathOperation.State state();

    @NotNull PathResult result();

    int iterations();

    boolean shouldRemove();

    @NotNull Set<PathDestination> getDestinations();

    @NotNull Set<PathNode> visitedNodes();

    @NotNull PathAgent agent();

    @NotNull ChunkCoordinateProvider searchArea();

    @NotNull NodeProvider nodeProvider();

    static PathOperation forAgent(@NotNull PathAgent agent, @NotNull Set<PathDestination> destinations,
                                  @NotNull ScoreCalculator calculator, @NotNull SuccessCondition successCondition,
                                  @NotNull NodeProvider provider, @NotNull DestinationSelector destinationSelector,
                                  @NotNull ChunkCoordinateProvider coordinateProvider) {
        Objects.requireNonNull(agent, "agent cannot be null!");
        Objects.requireNonNull(destinations, "destinations cannot be null!");
        Validate.isTrue(!destinations.isEmpty(), "destinations cannot be empty!");
        Objects.requireNonNull(calculator, "calculator cannot be null!");
        Objects.requireNonNull(successCondition,"terminationCondition cannot be null!");
        Objects.requireNonNull(provider, "provider cannot be null!");
        Objects.requireNonNull(destinationSelector, "destinationSelector cannot be null!");
        Objects.requireNonNull(coordinateProvider, "coordinateProvider cannot be null!");

        return new PathOperationImpl(agent, destinations, calculator, successCondition, provider,
                destinationSelector, coordinateProvider);
    }
}