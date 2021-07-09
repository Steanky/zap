package io.github.zap.vector.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class NodeSegment {
    private final NodeChunk parent;
    private final NodeLayer[] layers = new NodeLayer[16];
    private int emptyCount = 16;
    private final int parentIndex;

    NodeSegment(@NotNull NodeChunk parent, int parentIndex) {
        this.parent = parent;
        this.parentIndex = parentIndex;
    }

    void set(int y, @Nullable NodeLayer nodeLayer) {
        emptyCount = NodeUtils.setterHelper(nodeLayer, layers, y, emptyCount, parent::set, parentIndex);
    }

    @Nullable NodeLayer get(int y) {
        return layers[y];
    }
}
