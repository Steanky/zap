package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.util.VectorUtils;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

class PathNode implements Comparable<PathNode> {
    public final int x;
    public final int y;
    public final int z;
    public final int hash;

    public int cost;
    public PathNode next;

    public PathNode(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;

        hash = Objects.hash(x, y, z);
    }

    public PathNode(@NotNull Vector from) {
        this(from.getBlockX(), from.getBlockY(), from.getBlockZ());
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PathNode)) {
            return false;
        }
        else {
            PathNode other = (PathNode) obj;
            return x == other.x && y == other.y && z == other.z;
        }
    }

    @Override
    public String toString() {
        return "PathNode{x=" + x + ", y=" + y + ", z=" + z + ", hash=" + hash + ", cost=" + cost + "}";
    }

    @Override
    public int compareTo(@NotNull PathNode other) {
        return Float.compare(other.cost, cost);
    }

    public int distanceSquaredTo(@NotNull PathNode other) {
        return VectorUtils.distanceSquared(x, y, z, other.x, other.y, other.z);
    }

    public int distanceSquaredTo(int x, int y, int z) {
        return VectorUtils.distanceSquared(this.x, this.y, this.z, x, y, z);
    }
}
