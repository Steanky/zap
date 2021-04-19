package io.github.zap.arenaapi.vector;

class ChunkVectorImpl extends ChunkVector {
    private final int x;
    private final int z;

    ChunkVectorImpl(int x, int z) {
        this.x = x;
        this.z = z;
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
