package io.github.zap.arenaapi.vector;

import java.util.Objects;

public class ChunkVectorSource {
    private final int x;
    private final int z;
    private int hash = -1;

    public ChunkVectorSource(int x, int z) {
        this.x = x;
        this.z = z;
    }

    protected ChunkVectorSource() {
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
        if(obj.getClass() == ChunkVectorSource.class) {
            ChunkVectorSource other = (ChunkVectorSource) obj;
            return x == other.x && z == other.z;
        }

        return false;
    }
}
