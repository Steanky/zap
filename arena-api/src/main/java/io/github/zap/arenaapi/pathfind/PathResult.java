package io.github.zap.arenaapi.pathfind;

import io.github.zap.nms.common.pathfind.PathEntityWrapper;
import io.github.zap.vector.ImmutableWorldVector;
import io.github.zap.vector.VectorAccess;
import io.github.zap.vector.graph.ChunkGraph;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PathResult extends Iterable<PathNode> {
    @NotNull PathNode start();

    @NotNull PathOperation operation();

    @NotNull PathDestination destination();

    @NotNull VectorAccess lastDestination();

    @NotNull ChunkGraph<PathNode> visitedNodes();

    @NotNull List<PathNode> pathNodes();

    @NotNull PathOperation.State state();

    @NotNull PathEntityWrapper toPathEntity();
}
