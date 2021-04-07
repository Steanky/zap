package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.util.VectorUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PathNode {
    public final int x;
    public final int y;
    public final int z;
    private final int hash;

    Score score = new Score();
    PathNode parent;

    private PathNode(int x, int y, int z, int hash) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.hash = hash;
    }

    PathNode(int x, int y, int z) {
        this(x, y, z, Objects.hash(x, y, z));
    }

    PathNode(@NotNull Vector from) {
        this(from.getBlockX(), from.getBlockY(), from.getBlockZ());
    }

    PathNode(@NotNull Entity from) {
        this(from.getLocation().toVector());
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

    public int distanceSquaredTo(@NotNull PathNode other) {
        return VectorUtils.distanceSquared(x, y, z, other.x, other.y, other.z);
    }

    public int distanceSquaredTo(int x, int y, int z) {
        return VectorUtils.distanceSquared(this.x, this.y, this.z, x, y, z);
    }

    public PathNode add(int x, int y, int z) {
        return new PathNode(this.x + x, this.y + y, this.z + z);
    }

    public PathNode copy() {
        PathNode node = new PathNode(x, y, z, hash);
        node.score = score.copy();
        return node;
    }
}