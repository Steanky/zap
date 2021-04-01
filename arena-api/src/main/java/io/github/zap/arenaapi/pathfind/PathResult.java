package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface PathResult extends Iterable<PathNode> {
    @NotNull PathNode source();

    @NotNull PathDestination destination();

    @NotNull Set<PathNode> nodes();

    @NotNull PathOperation.State state();
}
