package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.ImmutableWorldVector;
import io.github.zap.vector.Positional;
import io.github.zap.vector.VectorAccess;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a single node in the graph, which may be linked to another node. Its coordinates generally represent
 * block coordinates.
 */
public class PathNode implements Positional {
    private final double x;
    private final double y;
    private final double z;

    private final int hash;
    private final ImmutableWorldVector position;

    int heapIndex = -1;
    final Score score;
    PathNode parent;

    private PathNode(Score score, PathNode parent, double x, double y, double z, int hash) {
        this.score = score;
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.z = z;
        this.hash = hash;
        position = VectorAccess.immutable(x, y, z);
    }

    PathNode(@NotNull Score score, @Nullable PathNode parent, double x, double y, double z) {
        this(score, parent, x, y, z, Objects.hash(x, y, z));
    }

    PathNode(@Nullable PathNode parent, @NotNull PathAgent agent) {
        this(new Score(), parent, agent.x(), agent.y(), agent.z());
    }

    PathNode(@Nullable PathNode parent, @NotNull Vector vector) {
        this(new Score(), parent, vector.getX(), vector.getY(), vector.getZ());
    }

    PathNode(@Nullable PathNode parent, double x, double y, double z) {
        this(new Score(), parent, x, y, z);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof PathNode) {
            PathNode otherNode = (PathNode) other;
            return x == otherNode.x && y == otherNode.y && z == otherNode.z;
        }

        return false;
    }

    @Override
    public String toString() {
        return "PathNode{x=" + x + ", y=" + y + ", z=" + z + ", score=" + score + "}";
    }

    public @NotNull PathNode chainOffset(double x, double y, double z) {
        return new PathNode(new Score(), this, this.x + x, this.y + y, this.z + z);
    }

    public @NotNull PathNode chain(@NotNull VectorAccess other) {
        return chain(other.x(), other.y(), other.z());
    }

    public @NotNull PathNode chain(double x, double y, double z) {
        return new PathNode(new Score(), this, x, y, z);
    }

    @Override
    public @NotNull VectorAccess position() {
        return position;
    }

    public @NotNull PathNode copy() {
        return new PathNode(score, parent, x, y, z, hash);
    }

    @NotNull PathNode reverse() {
        PathNode current = this;
        PathNode previous = null;

        while(current != null) {
            PathNode next = current.parent;
            current.parent = previous;
            previous = current;
            current = next;
        }

        return previous;
    }

    public boolean positionEquals(@NotNull PathNode other) {
        return x == other.x && y == other.y && z == other.z;
    }

    public boolean positionEquals(double x, double y, double z) {
        return this.x == x && this.y == y && this.z == z;
    }
}
