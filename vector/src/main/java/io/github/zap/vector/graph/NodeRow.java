package io.github.zap.vector.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

class NodeRow {
    private final NodeLayer parent;
    private final NodeLocation[] nodes = new NodeLocation[16];
    private int emptyCount = 16;
    private final int parentIndex;

    NodeRow(@NotNull NodeLayer parent, int parentIndex) {
        this.parent = parent;
        this.parentIndex = parentIndex;
    }

    void set(int z, @Nullable NodeLocation nodeLocation) {
        emptyCount = NodeUtils.setterHelper(nodeLocation, nodes, z, emptyCount, parent::set, parentIndex);
    }

    @Nullable NodeLocation get(int z) {
        return nodes[z];
    }
}
