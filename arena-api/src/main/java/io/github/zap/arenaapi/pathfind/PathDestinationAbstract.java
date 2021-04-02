package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

abstract class PathDestinationAbstract implements PathDestination {
    private final PathNode node;

    PathDestinationAbstract(@NotNull PathNode node) {
        this.node = node;
    }
    @Override
    public @NotNull PathNode node() {
        return node;
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof PathDestinationAbstract) {
            return ((PathDestinationAbstract) object).node.equals(node);
        }

        return false;
    }

    @Override
    public double destinationScore(@NotNull PathNode node) {
        return this.node.distanceSquaredTo(node);
    }
}
