package io.github.zap.arenaapi.pathfind.path;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.nms.common.entity.EntityBridge;
import io.github.zap.arenaapi.nms.common.pathfind.PathEntityWrapper;
import io.github.zap.arenaapi.nms.common.pathfind.PathPointWrapper;
import io.github.zap.arenaapi.pathfind.destination.PathDestination;
import io.github.zap.arenaapi.pathfind.operation.PathOperation;
import io.github.zap.vector.Vector3I;
import io.github.zap.vector.Vectors;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class PathResultImpl implements PathResult {
    private final PathNode start;
    private final PathOperation operation;
    private final PathDestination destination;
    private final List<PathNode> pathNodes = new ArrayList<>();
    private final PathOperation.State state;

    PathResultImpl(@NotNull PathNode start, @NotNull PathOperation operation, @NotNull PathDestination destination,
                   @NotNull PathOperation.State state) {
        this.start = start;
        this.operation = operation;
        this.destination = destination;
        this.state = state;

        while(start != null) {
            pathNodes.add(start);
            start = start.parent();
        }
    }

    @Override
    public @NotNull PathNode start() {
        return start;
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
            Vector3I offsetVector = node.getOffsetVector();
            PathPointWrapper point = bridge.makePathPoint(Vectors.add(node, offsetVector));
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
