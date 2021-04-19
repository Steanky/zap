package io.github.zap.arenaapi.vector2;

class ChunkVectorImpl extends ChunkVector {
    private final int x;
    private final int z;

    ChunkVectorImpl(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    int chunkX() {
        return x;
    }

    @Override
    int chunkZ() {
        return z;
    }
}
