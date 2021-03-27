package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

class BlockPathDestination implements PathDestination {
    private final PathNode node;

    BlockPathDestination(int x, int y, int z) {
        node = new PathNode(x, y, z);
    }

    @Override
    public @NotNull PathNode targetNode() {
        return node;
    }
}
