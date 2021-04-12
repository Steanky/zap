package io.github.zap.arenaapi.pathfind;

import java.util.Objects;

class ChunkVectorSourceImpl implements ChunkVectorSource {
    private final int x;
    private final int z;
    private final int hash;

    ChunkVectorSourceImpl(int x, int z) {
        this.x = x;
        this.z = z;

        hash = Objects.hash(x, z);
    }

    @Override
    public int chunkX() {
        return x;
    }

    @Override
    public int chunkZ() {
        return z;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ChunkVectorSourceImpl) {
            ChunkVectorSourceImpl other = (ChunkVectorSourceImpl) obj;
            return x == other.x && z == other.z;
        }

        return false;
    }
}
