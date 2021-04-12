package io.github.zap.arenaapi.pathfind;

import java.util.Objects;

class BlockVectorSourceImpl implements BlockVectorSource {
    private final int x;
    private final int y;
    private final int z;
    private final int hash;

    BlockVectorSourceImpl(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        hash = Objects.hash(x, y, z);
    }

    @Override
    public int blockX() {
        return x;
    }

    @Override
    public int blockY() {
        return y;
    }

    @Override
    public int blockZ() {
        return z;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BlockVectorSourceImpl) {
            BlockVectorSourceImpl other = (BlockVectorSourceImpl) obj;
            return x == other.x && y == other.y && z == other.z;
        }

        return false;
    }
}
