package io.github.zap.arenaapi.pathfind;

import net.minecraft.server.v1_16_R3.PathPoint;

public class PathNode {
    private final PathPoint handle;

    public PathNode(int x, int y, int z) {
        handle = new PathPoint(x, y, z);
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
}
