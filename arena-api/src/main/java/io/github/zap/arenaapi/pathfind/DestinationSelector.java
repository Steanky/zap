package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

/**
 * High level interface defining logic used to select one out of a number of destinations for pathfinding.
 */
@FunctionalInterface
public interface DestinationSelector {
    /**
     * Basic heuristic selector. Returns the nearest target for any given node, using a straight-line distance
     * comparison.
     */
    DestinationSelector CLOSEST = (operation, node) -> {
        double bestDistance = Double.MAX_VALUE;
        PathDestination bestDestination = null;

        for(PathDestination destination : operation.getDestinations()) {
            double sample = node.distanceSquared(destination.position());

            if(sample < bestDistance) {
                bestDistance = sample;
                bestDestination = destination;
            }
        }

        if(bestDestination == null) {
            throw new IllegalStateException("Unable to find a destination!");
        }

        return bestDestination;
    };

    /**
     * Selects a destination for the given PathOperation, which is currently expanding the given PathNode.
     *
     * This method may be called in an asynchronous context. To guard against this, it is necessary to perform one
     * or more of the following measures:
     *
     * <ul>
     *     <li>Apply appropriate synchronization techniques</li>
     *     <li>Check if the PathfinderEngine used by this operation is synchronous</li>
     *     <li>Ensure that the method is inherently thread-safe</li>
     * </ul>
     *
     * Exceptions thrown in this method will generally cause all PathOperations for a given context to be
     * cancelled, although this is implementation-dependent.
     *
     * This method should expect that the given PathOperation has at least one destination. If it does not, it may
     * be appropriate to throw an informative exception.
     *
     * @param operation The operation to select for
     * @param node The node the operation is currently expanding
     * @return The PathDestination to select
     */
    @NotNull PathDestination selectDestination(@NotNull PathOperation operation, @NotNull PathNode node);
}
