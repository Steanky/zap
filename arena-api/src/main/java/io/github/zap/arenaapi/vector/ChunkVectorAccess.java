package io.github.zap.arenaapi.vector;

public interface ChunkVectorAccess {
    int chunkX();

    int chunkZ();

    static ImmutableChunkVector immutable(int chunkX, int chunkZ) {
        return new ImmutableChunkVector(chunkX, chunkZ);
    }
}
