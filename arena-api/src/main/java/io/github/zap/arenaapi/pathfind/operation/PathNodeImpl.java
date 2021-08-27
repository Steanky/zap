package io.github.zap.arenaapi.pathfind.operation;

import io.github.zap.arenaapi.pathfind.path.PathNode;
import io.github.zap.vector.Vector3I;
import io.github.zap.vector.Vectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

class PathNodeImpl implements PathNode {
    private final int x;
    private final int y;
    private final int z;

    int heapIndex = -1;
    final Score score;
    PathNodeImpl parent;

    private final int hash;
    private Vector3I offsetVector = Vectors.ZERO;

    private PathNodeImpl(int x, int y, int z, Score score, int hash) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.hash = hash;
        this.score = score;
    }

    PathNodeImpl(int x, int y, int z) {
        this(x, y, z, new Score(), Objects.hash(x, y, z));
    }

    PathNodeImpl(@NotNull Vector3I blockPosition) {
        this(blockPosition.x(), blockPosition.y(), blockPosition.z());
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof PathNodeImpl otherNode) {
            return otherNode.x == this.x && otherNode.y == this.y && otherNode.z == this.z;
        }

        return false;
    }

    @Override
    public String toString() {
        return "PathNode{x=" + x + ", y=" + y + ", z=" + z + ", score=" + score + "}";
    }

    /**
     * Reverses the order of the linked PathNode objects. The returned PathNode will be the previously-parentless
     * node at the end of the chain.
     */
    @NotNull PathNodeImpl reverse() {
        PathNodeImpl current = this;
        PathNodeImpl previous = null;

        while(current != null) {
            PathNodeImpl next = current.parent;
            current.parent = previous;

            previous = current;
            current = next;
        }

        return previous;
    }

    @Override
    public int x() {
        return x;
    }

    @Override
    public int y() {
        return y;
    }

    @Override
    public int z() {
        return z;
    }

    @Override
    public void setOffsetVector(@NotNull Vector3I offsetVector) {
        this.offsetVector = Objects.requireNonNull(offsetVector, "offsetVector cannot be null");
    }

    @Override
    public @NotNull Vector3I getOffsetVector() {
        return offsetVector;
    }

    @Override
    public @Nullable PathNode parent() {
        return parent;
    }
}
