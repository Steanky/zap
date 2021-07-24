package io.github.zap.vector.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class NodeRow extends ArrayContainer<NodeLocation> {
    private final NodeLayer parent;
    private int emptyCount = 16;
    private final int parentIndex;

    NodeRow(@NotNull NodeLayer parent, int parentIndex) {
        super(new NodeLocation[16]);
        this.parent = parent;
        this.parentIndex = parentIndex;
    }

    void set(int z, @Nullable NodeLocation nodeLocation) {
        emptyCount = NodeUtils.setterHelper(nodeLocation, array, z, emptyCount, parent::set, parentIndex);
    }

    @Nullable NodeLocation get(int z) {
        return array[z];
    }
}
