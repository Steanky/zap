package io.github.zap.arenaapi.pathfind;

import io.github.zap.nms.common.pathfind.PathEntityWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

public interface PathResult extends Iterable<PathNode> {
    @NotNull PathNode start();

    @NotNull PathNode end();

    @NotNull PathOperation operation();

    @NotNull PathDestination destination();

    @NotNull Map<PathNode, PathNode> visitedNodes();

    @NotNull List<PathNode> pathNodes();

    @NotNull PathOperation.State state();

    @NotNull PathEntityWrapper toPathEntity();
}
