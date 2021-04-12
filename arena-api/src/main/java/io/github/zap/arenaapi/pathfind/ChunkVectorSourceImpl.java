package io.github.zap.arenaapi.pathfind;

import java.util.Objects;

class ChunkVectorSourceImpl implements ChunkVectorSource {
    private final int chunkX;
    private final int chunkZ;
    private final int hash;

    ChunkVectorSourceImpl(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;

        hash = Objects.hash(chunkX, chunkZ);
    }

    @Override
    public int chunkX() {
        return chunkX;
    }

    @Override
    public int chunkZ() {
        return chunkZ;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ChunkVectorSourceImpl) {
            ChunkVectorSourceImpl other = (ChunkVectorSourceImpl) obj;
            return chunkX == other.chunkX && chunkZ == other.chunkZ;
        }

        return false;
    }
}
