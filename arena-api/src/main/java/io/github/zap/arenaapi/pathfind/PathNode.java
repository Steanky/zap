package io.github.zap.arenaapi.pathfind;

public class PathNode {
    private final int x;
    private final int y;
    private final int z;

    private final int hash;

    public PathNode(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;

        hash = hashNode(x, y, z);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
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
            return hash == other.hash && x == other.x && y == other.y && z == other.z;
        }
    }

    //how NMS hashes pathnodes
    public static int hashNode(int x, int y, int z) {
        return y & 255 | (x & 32767) << 8 | (z & 32767) << 24 | (x < 0 ? -2147483648 : 0) | (z < 0 ? '耀' : 0);
    }
}
