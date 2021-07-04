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
    private final int x;
    private final int y;
    private final int z;

    private final int hash;
    private final ImmutableWorldVector position;

    int heapIndex = -1;
    final Score score;
    PathNode parent;

    private PathNode(Score score, PathNode parent, int x, int y, int z, int hash) {
        this.score = score;
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.z = z;
        this.hash = hash;
        position = VectorAccess.immutable(x, y, z);
    }

    public PathNode(int x, int y, int z) {
        this(new Score(), null, x, y, z, Objects.hash(x, y, z));
    }

    public PathNode(@NotNull Score score, @Nullable PathNode parent, int x, int y, int z) {
        this(score, parent, x, y, z, Objects.hash(x, y, z));
    }

    public PathNode(@Nullable PathNode parent, @NotNull PathAgent agent) {
        this(new Score(), parent, agent.blockX(), agent.blockY(), agent.blockZ());
    }

    public PathNode(@Nullable PathNode parent, @NotNull Vector vector) {
        this(new Score(), parent, vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public PathNode(@Nullable PathNode parent, int x, int y, int z) {
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

    public @NotNull PathNode chain(@NotNull VectorAccess other) {
        return chain(other.blockX(), other.blockY(), other.blockZ());
    }

    public @NotNull PathNode chain(int x, int y, int z) {
        return new PathNode(new Score(), this, x, y, z);
    }

    @Override
    public @NotNull VectorAccess position() {
        return position;
    }

    public @NotNull PathNode copy() {
        return new PathNode(score, parent, x, y, z, hash);
    }

    public @NotNull PathNode reverse() {
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
