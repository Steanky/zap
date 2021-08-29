package io.github.zap.arenaapi.pathfind.operation;

import io.github.zap.arenaapi.pathfind.agent.PathAgent;
import io.github.zap.arenaapi.pathfind.chunk.ChunkBounds;
import io.github.zap.arenaapi.pathfind.context.PathfinderContext;
import io.github.zap.arenaapi.pathfind.destination.PathDestination;
import io.github.zap.arenaapi.pathfind.path.PathNode;
import io.github.zap.arenaapi.pathfind.path.PathResult;
import io.github.zap.arenaapi.pathfind.step.NodeExplorer;
import io.github.zap.vector.graph.ChunkGraph;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an individual pathfinding operation.
 */
public interface PathOperation {
    enum State {
        STARTED,
        SUCCEEDED,
        FAILED
    }

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
    @NotNull PathDestination destination();

    /**
     * Returns a map of all of the nodes visited by this PathOperation so far.
     */
    @NotNull ChunkGraph<? extends PathNode> visitedNodes();

    /**
     * Returns the PathAgent that this PathOperation is using.
     */
    @NotNull PathAgent agent();

    /**
     * Returns the chunk coordinates this operation encompasses.
     */
    @NotNull ChunkBounds searchArea();

    /**
     * Returns the NodeProvider instance used by this object. This is used to create explorable nodes for the
     * pathfinding algorithm.
     */
    @NotNull NodeExplorer nodeExplorer();

    @Nullable PathNode currentNode();
}