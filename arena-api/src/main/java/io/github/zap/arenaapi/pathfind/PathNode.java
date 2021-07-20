package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.Vector3I;
import io.github.zap.vector.Vectors;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a single node in the graph, which may be linked to another node. Its coordinates generally represent
 * block coordinates.
 */
public class PathNode implements Vector3I {
    private final Vector3I position;

    int heapIndex = -1;
    final Score score;
    PathNode parent;
    PathNode child;

    private final int hash;

    public PathNode(@NotNull Vector3I vector) {
        position = vector;
        hash = Objects.hash(position);
        score = new Score();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof PathNode otherNode) {
            return otherNode.position.equals(position);
        }

        return false;
    }

    @Override
    public String toString() {
        return "PathNode{x=" + position.x() + ", y=" + position.y() + ", z=" + position.z() + ", score=" + score + "}";
    }

    /**
     * Reverses the order of the linked PathNode objects. The returned PathNode will be the previously-parentless
     * node at the end of the chain.
     */
    public @NotNull PathNode reverse() {
        PathNode current = this;
        PathNode previous = null;

        while(current != null) {
            PathNode next = current.parent;
            current.parent = previous;

            if(previous != null) {
                previous.child = current;
            }

            previous = current;
            current = next;
        }

        previous.child = null;
        return previous;
    }

    public @NotNull PathNode chain(int x, int y, int z) {
        PathNode node = new PathNode(Vectors.of(x, y, z));
        node.parent = this;
        child = node;
        return node;
    }

    @Override
    public int x() {
        return position.x();
    }

    @Override
    public int y() {
        return position.y();
    }

    @Override
    public int z() {
        return position.z();
    }
}
