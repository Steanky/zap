package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

class PathDestinationImpl implements PathDestination {
    private final PathNode node;

    PathDestinationImpl(@NotNull PathNode node) {
        this.node = node;
    }

    @Override
    public @NotNull PathNode node() {
        return node.copy();
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof PathDestinationImpl) {
            return ((PathDestinationImpl) object).node.equals(node);
        }

        return false;
    }

    @Override
    public double destinationScore(@NotNull PathNode node) {
        return this.node.distanceSquaredTo(node);
    }
}
