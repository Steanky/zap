package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.pathfind.traversal.NodeGraph;
import io.github.zap.nms.common.entity.EntityBridge;
import io.github.zap.nms.common.pathfind.PathEntityWrapper;
import io.github.zap.nms.common.pathfind.PathPointWrapper;
import io.github.zap.vector.VectorAccess;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class PathResultImpl implements PathResult {
    private final PathNode start;
    private final PathNode end;
    private final PathOperation operation;
    private final PathDestination destination;
    private final NodeGraph visitedNodes;
    private final List<PathNode> pathNodes = new ArrayList<>();
    private final PathOperation.State state;

    PathResultImpl(@NotNull PathNode start, @NotNull PathOperation operation,
                   @NotNull NodeGraph visitedNodes, @NotNull PathDestination destination,
                   @NotNull PathOperation.State state) {
        this.start = start;
        this.operation = operation;
        this.visitedNodes = visitedNodes;
        this.destination = destination;
        this.state = state;

        PathNode end = null;
        while(start != null) {
            pathNodes.add(start);
            end = start;
            start = start.parent;
        }

        this.end = end;
    }

    @Override
    public @NotNull PathNode start() {
        return start;
    }

    @Override
    public @NotNull PathNode end() {
        return end;
    }

    @Override
    public @NotNull PathOperation operation() {
        return operation;
    }

    @Override
    public @NotNull PathDestination destination() {
        return destination;
    }

    @Override
    public @NotNull NodeGraph visitedNodes() {
        return visitedNodes;
    }

    @Override
    public @NotNull List<PathNode> pathNodes() {
        return pathNodes;
    }

    @Override
    public @NotNull PathOperation.State state() {
        return state;
    }

    @Override
    public @NotNull PathEntityWrapper toPathEntity() {
        List<PathPointWrapper> wrapper = new ArrayList<>();
        EntityBridge bridge = ArenaApi.getInstance().getNmsBridge().entityBridge();

        PathPointWrapper previous = null;
        for(PathNode node : pathNodes) {
            PathPointWrapper point = bridge.makePathPoint(node);
            point.setParent(previous);

            wrapper.add(point);
            previous = point;
        }

        return bridge.makePathEntity(wrapper, destination, state == PathOperation.State.SUCCEEDED);
    }

    @NotNull
    @Override
    public Iterator<PathNode> iterator() {
        return pathNodes.iterator();
    }
}
