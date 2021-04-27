package io.github.zap.arenaapi.vector;

public class ChunkVector implements ChunkVectorAccess {
    private final int x;
    private final int z;

    public ChunkVector(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 79 + x;
        hash = hash * 79 + z;
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof ChunkVector) {
            ChunkVector otherVector = (ChunkVector) other;
            return otherVector.x == x && otherVector.z == z;
        }

        return false;
    }

    @Override
    public int chunkX() {
        return x;
    }

    @Override
    public int chunkZ() {
        return z;
    }
}
