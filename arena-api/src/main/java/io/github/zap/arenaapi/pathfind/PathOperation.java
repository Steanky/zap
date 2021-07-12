package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.VectorAccess;
import io.github.zap.vector.graph.ChunkGraph;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

/**
 * Represents an individual pathfinding operation.
 */
public interface PathOperation {
    enum State {
        NOT_STARTED,
        STARTED,
        SUCCEEDED,
        FAILED
    }

    /**
     * Puts this PathOperation into a "ready" state (sets the state to STARTED from NOT_STARTED, and performs
     * initialization tasks).
     * @param context The current context
     */
    void init(@NotNull PathfinderContext context);

    /**
     * Used to compare two PathOperation objects, to see if they may be merged for optimization purposes.
     */
    boolean comparableTo(@NotNull PathOperation other);

    /**
     * Performs one A* (or equivalent algorithm) iteration for the given context. Calling this method may throw an
     * exception if the pathfinder's state is not STARTED.
     * @param context The current context
     * @return False if the path operation has not completed, true otherwise
     */
    boolean step(@NotNull PathfinderContext context);

    /**
     * Gets the current state of the PathOperation object.
     * @return The state of the PathOperation
     */
    @NotNull PathOperation.State state();

    /**
     * The PathResult encapsulating the result of this operation. May throw an exception if the state is != SUCCEEDED ||
     * FAILED
     * @return The PathResult object
     */
    @NotNull PathResult result();

    /**
     * Returns the number of iterations this operation should be given at a time. This can be used to "load balance"
     * multiple PathOperations by a PathfinderEngine. This number is a suggestion; it might not always be honored.
     * @return The number of iterations this operation wants to receive
     */
    int iterations();

    /**
     * Returns a set of PathDestination objects corresponding to the possible destinations for this PathOperation.
     */
    @NotNull Set<? extends PathDestination> getDestinations();

    /**
     * Returns a map of all of the nodes visited by this PathOperation so far.
     */
    @NotNull ChunkGraph<PathNode> visitedNodes();

    /**
     * Returns the PathAgent that this PathOperation is using.
     */
    @NotNull PathAgent agent();

    /**
     * Returns the chunk coordinates this operation encompasses.
     */
    @NotNull ChunkCoordinateProvider searchArea();

    /**
     * Returns the NodeProvider instance used by this object. This is used to create explorable nodes for the
     * pathfinding algorithm.
     */
    @NotNull NodeExplorer nodeProvider();

    @Nullable PathDestination bestDestination();

    @Nullable PathNode currentNode();

    boolean mergeValid(@NotNull PathOperation other);

    boolean allowMerges();

    static PathOperation forAgent(@NotNull PathAgent agent, @NotNull Set<? extends PathDestination> destinations,
                                  @NotNull HeuristicCalculator calculator, @NotNull SuccessCondition successCondition,
                                  @NotNull NodeExplorer provider, @NotNull DestinationSelector destinationSelector,
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

    static PathOperation forEntityWalking(@NotNull Entity entity, @NotNull Set<? extends PathDestination> destinations, int loadRadius) {
        return forAgent(PathAgent.fromEntity(entity), destinations, HeuristicCalculator.DISTANCE_ONLY, SuccessCondition.WITHIN_BLOCK,
                new DefaultWalkNodeExplorer(AversionCalculator.DEFAULT_WALK), DestinationSelector.CLOSEST,
                ChunkCoordinateProvider.squareFromCenter(VectorAccess.immutable(entity.getLocation().toVector()), loadRadius));
    }

    static PathOperation forEntityWalking(@NotNull Entity entity, @NotNull Set<? extends PathDestination> destinations,
                                          ChunkCoordinateProvider searchArea) {
        return forAgent(PathAgent.fromEntity(entity), destinations, HeuristicCalculator.DISTANCE_ONLY,
                SuccessCondition.WITHIN_BLOCK, new DefaultWalkNodeExplorer(AversionCalculator.DEFAULT_WALK),
                DestinationSelector.CLOSEST, searchArea);
    }

    static PathOperation forEntityWalking(@NotNull Entity entity, @NotNull Set<? extends PathDestination> destinations,
                                          ChunkCoordinateProvider searchArea, double targetDeviation) {
        return forAgent(PathAgent.fromEntity(entity), destinations, HeuristicCalculator.DISTANCE_ONLY,
                SuccessCondition.whenWithin(targetDeviation * targetDeviation),
                new DefaultWalkNodeExplorer(AversionCalculator.DEFAULT_WALK), DestinationSelector.CLOSEST, searchArea);
    }

    static PathOperation forEntityWalking(@NotNull Entity entity, @NotNull Set<? extends PathDestination> destinations,
                                          int loadRadius, double targetDeviation) {
        return forAgent(PathAgent.fromEntity(entity), destinations, HeuristicCalculator.DISTANCE_ONLY,
                SuccessCondition.whenWithin(targetDeviation * targetDeviation),
                new DefaultWalkNodeExplorer(AversionCalculator.DEFAULT_WALK), DestinationSelector.CLOSEST,
                ChunkCoordinateProvider.squareFromCenter(VectorAccess.immutable(entity.getLocation().toVector()), loadRadius));
    }
}