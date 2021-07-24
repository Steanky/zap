package io.github.zap.vector.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class NodeSegment extends ArrayContainer<NodeLayer> {
    private final NodeChunk parent;
    private int emptyCount = 16;
    private final int parentIndex;

    NodeSegment(@NotNull NodeChunk parent, int parentIndex) {
        super(new NodeLayer[16]);
        this.parent = parent;
        this.parentIndex = parentIndex;
    }

    void set(int y, @Nullable NodeLayer nodeLayer) {
        emptyCount = NodeUtils.setterHelper(nodeLayer, array, y, emptyCount, parent::set, parentIndex);
    }

    @Nullable NodeLayer get(int y) {
        return array[y];
    }
}
