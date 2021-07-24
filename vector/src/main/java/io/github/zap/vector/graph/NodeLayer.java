package io.github.zap.vector.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class NodeLayer extends ArrayContainer<NodeRow> {
    private final NodeSegment parent;
    private int emptyCount = 16;
    private final int parentIndex;

    NodeLayer(@NotNull NodeSegment parent, int parentIndex) {
        super(new NodeRow[16]);
        this.parent = parent;
        this.parentIndex = parentIndex;
    }

    void set(int x, @Nullable NodeRow nodeLocation) {
        emptyCount = NodeUtils.setterHelper(nodeLocation, array, x, emptyCount, parent::set, parentIndex);
    }

    @Nullable NodeRow get(int x) {
        return array[x];
    }
}
