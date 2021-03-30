package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.*;

class PathResultImpl implements PathResult {
    private static class ResultIterator implements Iterator<PathNode> {
        private PathNode current;

        public ResultIterator(PathNode current) {
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

    private final PathNode source;
    private final PathDestination destination;
    private final Set<PathNode> nodes;

    PathResultImpl(@NotNull PathNode source, @NotNull PathDestination destination) {
        this.source = source;
        this.destination = destination;
        nodes = new LinkedHashSet<>();
    }

    @Override
    public @NotNull PathNode source() {
        return source;
    }

    @Override
    public @NotNull PathDestination destination() {
        return destination;
    }

    @Override
    public @NotNull Set<PathNode> nodes() {
        return nodes;
    }

    @NotNull
    @Override
    public Iterator<PathNode> iterator() {
        return new ResultIterator(source);
    }
}
