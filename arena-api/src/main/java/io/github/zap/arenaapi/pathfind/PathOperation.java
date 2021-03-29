package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

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
}