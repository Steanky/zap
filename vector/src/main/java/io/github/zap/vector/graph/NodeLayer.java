package io.github.zap.vector.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

class NodeLayer {
    private final NodeSegment parent;
    private final NodeRow[] rows = new NodeRow[16];
    private int emptyCount = 16;
    private final int parentIndex;

    NodeLayer(@NotNull NodeSegment parent, int parentIndex) {
        this.parent = parent;
        this.parentIndex = parentIndex;
    }

    void set(int x, @Nullable NodeRow nodeLocation) {
        emptyCount = NodeUtils.setterHelper(nodeLocation, rows, x, emptyCount, parent::set, parentIndex);
    }

    @Nullable NodeRow get(int x) {
        return rows[x];
    }
}
