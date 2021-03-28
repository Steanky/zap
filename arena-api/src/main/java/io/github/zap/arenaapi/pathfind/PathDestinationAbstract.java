package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

class PathDestinationAbstract implements PathDestination {
    private final PathNode node;

    PathDestinationAbstract(@NotNull PathNode node) {
        this.node = node;
    }

    @Override
    public @NotNull PathNode targetNode() {
        return node;
    }

    @Override
    public int hashCode() {
        return node.hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PathDestinationAbstract) {
            PathDestinationAbstract other = (PathDestinationAbstract) obj;
            return other.node.equals(node);
        }

        return false;
    }
}
