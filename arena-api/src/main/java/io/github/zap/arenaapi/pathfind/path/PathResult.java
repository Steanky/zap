package io.github.zap.arenaapi.pathfind.path;

import io.github.zap.arenaapi.nms.common.pathfind.PathEntityWrapper;
import io.github.zap.arenaapi.pathfind.destination.PathDestination;
import io.github.zap.arenaapi.pathfind.operation.PathOperation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PathResult extends Iterable<PathNode> {
    @NotNull PathNode start();

    @NotNull PathOperation operation();

    @NotNull PathDestination destination();

    @NotNull List<PathNode> pathNodes();

    @NotNull PathOperation.State state();

    @NotNull PathEntityWrapper toPathEntity();
}
