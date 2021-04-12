package io.github.zap.arenaapi.pathfind;

import java.util.Objects;

class WorldVectorSourceImpl implements WorldVectorSource {
    private final double x;
    private final double y;
    private final double z;

    private final int bx;
    private final int by;
    private final int bz;

    private final int hash;

    WorldVectorSourceImpl(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        bx = (int)x;
        by = (int)y;
        bz = (int)z;

        hash = Objects.hash(x, y, z);
    }

    @Override
    public int blockX() {
        return bx;
    }

    @Override
    public int blockY() {
        return by;
    }

    @Override
    public int blockZ() {
        return bz;
    }

    @Override
    public double worldX() {
        return x;
    }

    @Override
    public double worldY() {
        return y;
    }

    @Override
    public double worldZ() {
        return z;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof WorldVectorSourceImpl) {
            WorldVectorSourceImpl other = (WorldVectorSourceImpl) obj;
            return x == other.x && y == other.y && z == other.z;
        }

        return false;
    }
}
