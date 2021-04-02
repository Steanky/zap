package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface PathResult extends Iterable<PathNode> {
    @NotNull PathNode start();

    @NotNull PathNode end();

    @NotNull PathOperation operation();

    @NotNull PathDestination destination();

    @NotNull Set<PathNode> visitedNodes();

    @NotNull PathOperation.State state();
}
