package io.github.zap.arenaapi.vector;

abstract class ChunkVector<T extends ChunkVector<T>> implements ChunkVectorAccess {
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + chunkX();
        hash = 79 * hash + chunkZ();
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof ChunkVector) {
            ChunkVector<?> otherVector = (ChunkVector<?>) other;
            return chunkX() == otherVector.chunkX() && chunkZ() == otherVector.chunkZ();
        }

        return false;
    }
}
