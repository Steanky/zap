package io.github.zap.arenaapi.pathfind;

import net.minecraft.server.v1_16_R3.PathPoint;

public class PathNode {
    private final PathPoint handle;

    public PathNode(int x, int y, int z) {
        handle = new PathPoint(x, y, z);
    }

    public PathNode(PathPoint from) {
        this.handle = from;
    }

    public int getX() {
        return handle.a;
    }

    public int getY() {
        return handle.b;
    }

    public int getZ() {
        return handle.c;
    }

    @Override
    public int hashCode() {
        return handle.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PathNode)) {
            return false;
        }
        else {
            PathNode other = (PathNode) obj;
            return getX() == other.getX() && getY() == other.getY() && getZ() == other.getZ();
        }
    }
}
