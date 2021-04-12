package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.util.VectorUtils;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a single node in the graph, which may be linked to another node. Its coordinates generally represent
 * block coordinates, but may point to a location within any particular block (ex. [0.5, 0.5, 0.5] referring to the
 * exact center of the block at [0, 0, 0]).
 *
 * Since NMS PathPoint objects require integers, when converting PathNode objects will cast their double fields to
 * int.
 */
public class PathNode {
    public final double x;
    public final double y;
    public final double z;
    public final int blockX;
    public final int blockY;
    public final int blockZ;
    private final int hash;

    final Score score;
    PathNode parent;

    private PathNode(Score score, PathNode parent, double x, double y, double z, int hash) {
        this.score = score;
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockX = (int)x;
        this.blockY = (int)y;
        this.blockZ = (int)z;
        this.hash = hash;
    }

    PathNode(@NotNull Score score, @Nullable PathNode parent, double x, double y, double z) {
        this(score, parent, x, y, z, Objects.hash(x, y, z));
    }

    PathNode(@Nullable PathNode parent, @NotNull PathAgent agent) {
        this(new Score(), parent, agent.position().getX(), agent.position().getY(), agent.position().getZ());
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
            PathNode node = (PathNode) other;
            return x == node.x && y == node.y && z == node.z;
        }

        return false;
    }

    @Override
    public String toString() {
        return "PathNode{x=" + x + ", y=" + y + ", z=" + z + ", score=" + score + "}";
    }

    public double distanceSquaredTo(@NotNull PathNode other) {
        return VectorUtils.distanceSquared(x, y, z, other.x, other.y, other.z);
    }

    public PathNode add(double x, double y, double z) {
        return new PathNode(new Score(), this, this.x + x, this.y + y, this.z + z);
    }

    public PathNode link(double x, double y, double z) {
        return new PathNode(new Score(), this, x, y, z);
    }

    public PathNode copy() {
        return new PathNode(score, parent, x, y, z, hash);
    }
}
