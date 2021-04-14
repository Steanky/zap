package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.*;

class PathResultImpl implements PathResult {
    private static class ResultIterator implements Iterator<PathNode> {
        private PathNode current;

        private ResultIterator(PathNode current) {
            this.current = current;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public PathNode next() {
            PathNode save = current;
            current = current.parent;
            return save;
        }
    }

    private final PathNode start;
    private final PathNode end;
    private final PathOperation operation;
    private final PathDestination destination;
    private final Map<PathNode, PathNode> visitedNodes;
    private final List<PathNode> pathNodes = new ArrayList<>();
    private final PathOperation.State state;

    PathResultImpl(@NotNull PathNode start, @NotNull PathOperation operation,
                   @NotNull Map<PathNode, PathNode> visitedNodes, @NotNull PathDestination destination,
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
    public @NotNull Map<PathNode, PathNode> visitedNodes() {
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

    @NotNull
    @Override
    public Iterator<PathNode> iterator() {
        return new ResultIterator(end);
    }
}
