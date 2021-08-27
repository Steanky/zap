package io.github.zap.arenaapi.pathfind.path;

import io.github.zap.arenaapi.pathfind.destination.PathDestination;
import io.github.zap.arenaapi.pathfind.operation.PathOperation;
import org.jetbrains.annotations.NotNull;

public final class PathResults {
    public static @NotNull PathResult basic(@NotNull PathNode start, @NotNull PathOperation operation,
                                            @NotNull PathDestination destination, @NotNull PathOperation.State state) {
        return new PathResultImpl(start, operation, destination, state);
    }
}
