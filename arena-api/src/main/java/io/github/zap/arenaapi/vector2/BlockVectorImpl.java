package io.github.zap.arenaapi.vector2;

class BlockVectorImpl extends BlockVector {
    private final int x;
    private final int y;
    private final int z;

    BlockVectorImpl(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int blockX() {
        return x;
    }

    @Override
    public int blockY() {
        return y;
    }

    @Override
    public int blockZ() {
        return z;
    }
}
