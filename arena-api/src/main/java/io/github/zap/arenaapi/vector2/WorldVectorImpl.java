package io.github.zap.arenaapi.vector2;

class WorldVectorImpl extends WorldVector {
    private final double x;
    private final double y;
    private final double z;

    WorldVectorImpl(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public double worldX() {
        return x;
    }

    @Override
    public double worldY() {
        return y;
    }

    @Override
    public double worldZ() {
        return z;
    }
}
