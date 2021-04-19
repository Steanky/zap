package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.WorldVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a single node in the graph, which may be linked to another node. Its coordinates generally represent
 * block coordinates, but may point to a location within any particular block (ex. [0.5, 0.5, 0.5] referring to the
 * exact center of the block at [0, 0, 0]).
 */
public class PathNode {
    public final double x;
    public final double y;
    public final double z;

    public final int blockX;
    public final int blockY;
    public final int blockZ;

    private final int hash;
    private WorldVector vectorSource = null;

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
        this(new Score(), parent, agent.position().worldX(), agent.position().worldY(), agent.position().worldZ());
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
            return position().equals(((PathNode) other).position());
        }

        return false;
    }

    @Override
    public String toString() {
        return "PathNode{x=" + x + ", y=" + y + ", z=" + z + ", score=" + score + "}";
    }

    public @NotNull PathNode add(double x, double y, double z) {
        return new PathNode(new Score(), this, this.x + x, this.y + y, this.z + z);
    }

    public @NotNull PathNode link(double x, double y, double z) {
        return new PathNode(new Score(), this, x, y, z);
    }

    public @NotNull PathNode link(@NotNull WorldVector source) {
        return link(source.worldX(), source.worldY(), source.worldZ());
    }

    public @NotNull PathNode copy() {
        return new PathNode(score, parent, x, y, z, hash);
    }

    public @NotNull WorldVector position() {
        return new WorldVector(x, y, z);
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
}
