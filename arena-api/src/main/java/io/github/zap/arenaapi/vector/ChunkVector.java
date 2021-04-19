package io.github.zap.arenaapi.vector;

import java.util.Objects;

public class ChunkVector {
    private final int x;
    private final int z;
    private int hash = -1;

    public ChunkVector(int x, int z) {
        this.x = x;
        this.z = z;
    }

    protected ChunkVector() {
        x = 0;
        z = 0;
    }

    public int chunkX() {
        return x;
    }

    public int chunkZ() {
        return z;
    }

    @Override
    public int hashCode() {
        return hash == -1 ? hash = Objects.hash(x, z) : hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() == ChunkVector.class) {
            ChunkVector other = (ChunkVector) obj;
            return x == other.x && z == other.z;
        }

        return false;
    }

    @Override
    public String toString() {
        return "ChunkVectorSource{x=" + x + ", z=" + z + "}";
    }
}
