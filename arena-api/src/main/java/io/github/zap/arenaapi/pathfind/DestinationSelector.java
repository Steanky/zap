package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

public interface DestinationSelector {
    DestinationSelector CLOSEST = (operation, node) -> {
        double bestDistance = Double.MAX_VALUE;
        PathDestination bestDestination = null;

        for(PathDestination destination : operation.getDestinations()) {
            double sample = node.distanceSquaredTo(destination.node());

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

    @NotNull PathDestination selectDestinationFor(@NotNull PathOperation operation, @NotNull PathNode node);
}
