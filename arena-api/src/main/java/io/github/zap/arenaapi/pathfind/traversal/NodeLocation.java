package io.github.zap.arenaapi.pathfind.traversal;

import io.github.zap.arenaapi.pathfind.PathNode;
import io.github.zap.arenaapi.pathfind.PathOperation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class NodeLocation {
    private final NodeRow parent;
    private final PathNode node;
    private final PathOperation operation;
    private final int parentIndex;

    NodeLocation(@NotNull NodeRow parent, @NotNull PathNode node, @NotNull PathOperation operation, int parentIndex) {
        this.parent = parent;
        this.node = node;
        this.operation = operation;
        this.parentIndex = parentIndex;
    }
}
