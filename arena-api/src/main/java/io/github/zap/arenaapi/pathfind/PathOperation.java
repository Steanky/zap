package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.VectorAccess;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public interface PathOperation {
    enum State {
        NOT_STARTED,
        STARTED,
        SUCCEEDED,
        FAILED
    }

    void init(@NotNull PathfinderContext context);

    boolean comparableTo(@NotNull PathOperation other);

    boolean step(@NotNull PathfinderContext context);

    @NotNull PathOperation.State state();

    @NotNull PathResult result();

    int iterations();

    @NotNull Set<? extends PathDestination> getDestinations();

    @NotNull Map<PathNode, PathNode> visitedNodes();

    @NotNull PathAgent agent();

    @NotNull ChunkCoordinateProvider searchArea();

    @NotNull NodeProvider nodeProvider();

    static PathOperation forAgent(@NotNull PathAgent agent, @NotNull Set<? extends PathDestination> destinations,
                                  @NotNull HeuristicCalculator calculator, @NotNull SuccessCondition successCondition,
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

    static PathOperation forEntityWalking(@NotNull Entity entity, @NotNull Set<? extends PathDestination> destinations, int radius) {
        return forAgent(PathAgent.fromEntity(entity), destinations, HeuristicCalculator.DISTANCE_ONLY, SuccessCondition.WITHIN_BLOCK,
                new DefaultWalkNodeProvider(AversionCalculator.DEFAULT_WALK), DestinationSelector.CLOSEST,
                ChunkCoordinateProvider.squareFromCenter(VectorAccess.immutable(entity.getLocation().toVector()), radius));
    }

    static PathOperation forEntityWalking(@NotNull Entity entity, @NotNull Set<? extends PathDestination> destinations,
                                          int loadRadius, int targetDeviation) {
        return forAgent(PathAgent.fromEntity(entity), destinations, HeuristicCalculator.DISTANCE_ONLY,
                SuccessCondition.whenWithin(targetDeviation * targetDeviation),
                new DefaultWalkNodeProvider(AversionCalculator.DEFAULT_WALK), DestinationSelector.CLOSEST,
                ChunkCoordinateProvider.squareFromCenter(VectorAccess.immutable(entity.getLocation().toVector()), loadRadius));
    }
}