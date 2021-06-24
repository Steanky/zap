package io.github.zap.vector;

public interface ChunkVectorAccess {
    int chunkX();

    int chunkZ();

    static ImmutableChunkVector immutable(int chunkX, int chunkZ) {
        return new ImmutableChunkVector(chunkX, chunkZ);
    }
}
