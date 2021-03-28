package io.github.zap.arenaapi.pathfind;

import net.minecraft.server.v1_16_R3.PathPoint;

import java.util.Objects;

public class PathNode {
    public final int x;
    public final int y;
    public final int z;

    private final int hash;

    public PathNode next;
    public float distanceToNext;

    public PathNode(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;

        hash = Objects.hash(x, y, z);
    }

    public PathPoint toNms() {
        return new PathPoint(x, y, z);
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
}
