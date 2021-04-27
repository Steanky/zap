package io.github.zap.arenaapi.vector;

public class ImmutableChunkVector extends ChunkVector<ImmutableChunkVector> {
    private final int x;
    private final int z;

    public ImmutableChunkVector(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public String toString() {
        return "ChunkVector{x=" + x + ", z=" + z + "}";
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
