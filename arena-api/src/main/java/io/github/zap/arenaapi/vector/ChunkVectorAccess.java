package io.github.zap.arenaapi.vector;

public interface ChunkVectorAccess {
    int chunkX();

    int chunkZ();

    static ChunkVectorAccess immutable(int chunkX, int chunkZ) {
        return new ChunkVector(chunkX, chunkZ);
    }
}
