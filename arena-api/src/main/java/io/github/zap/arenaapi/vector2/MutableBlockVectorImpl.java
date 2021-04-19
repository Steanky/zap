package io.github.zap.arenaapi.vector2;

class MutableBlockVectorImpl extends MutableBlockVector {
    private int x;
    private int y;
    private int z;

    MutableBlockVectorImpl(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    void setBlockX(int x) {
        this.x = x;
    }

    @Override
    void setBlockY(int y) {
        this.y = y;
    }

    @Override
    void setBlockZ(int z) {
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
