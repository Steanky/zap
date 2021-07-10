package io.github.zap.vector;

import org.jetbrains.annotations.NotNull;

public class MutableWorldVector extends WorldVector<MutableWorldVector> {
    private double x;
    private double y;
    private double z;

    public MutableWorldVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MutableWorldVector() {
        this(0, 0, 0);
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public double x() {
        return x;
    }

    @Override
    public double y() {
        return y;
    }

    @Override
    public double z() {
        return z;
    }

    @Override
    public @NotNull MutableWorldVector copyVector() {
        return new MutableWorldVector(x, y, z);
    }

    @Override
    @NotNull MutableWorldVector operation(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public String toString() {
        return "MutableWorldVector{x=" + x + ", y=" + y + ", z=" + z + "}";
    }

    @Override
    public MutableWorldVector asMutable() {
        return this;
    }
}
