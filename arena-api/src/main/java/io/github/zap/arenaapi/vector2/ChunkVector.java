package io.github.zap.arenaapi.vector2;

import java.util.Objects;

public abstract class ChunkVector {
    protected boolean hasHash;
    protected int hash;

    public abstract int chunkX();

    public abstract int chunkZ();

    @Override
    public int hashCode() {
        return Objects.hash(chunkX(), chunkZ());
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof ChunkVector) {
            ChunkVector otherVector = (ChunkVector) other;
            return otherVector.chunkX() == chunkX() && otherVector.chunkZ() == chunkZ();
        }

        return false;
    }

    public static ChunkVector immutable(int x, int z) {
        return new ChunkVectorImpl(x, z);
    }
}
