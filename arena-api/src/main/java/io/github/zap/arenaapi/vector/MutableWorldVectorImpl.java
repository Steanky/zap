package io.github.zap.arenaapi.vector;

class MutableWorldVectorImpl extends MutableWorldVector {
    private double x;
    private double y;
    private double z;

    MutableWorldVectorImpl(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void setWorldX(double x) {
        this.x = x;
    }

    @Override
    public void setWorldY(double y) {
        this.y = y;
    }

    @Override
    public void setWorldZ(double z) {
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
