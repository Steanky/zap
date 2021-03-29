package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DestinationSelector {
    DestinationSelector SIMPLE = (operation, node) -> {
        int bestDistance = Integer.MAX_VALUE;
        PathDestination bestDestination = null;

        for(PathDestination destination : operation.getDestinations()) {
            int sample = node.distanceSquaredTo(destination.node());

            if(sample < bestDistance) {
                bestDistance = sample;
                bestDestination = destination;
            }
        }

        return bestDestination;
    };

    @Nullable PathDestination selectDestinationFor(@NotNull PathOperation operation, @NotNull PathNode node);
}
