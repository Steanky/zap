package io.github.zap.vector;

import org.jetbrains.annotations.NotNull;

public class ImmutableWorldVector extends WorldVector<ImmutableWorldVector> {
    private final double x;
    private final double y;
    private final double z;

    public ImmutableWorldVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ImmutableWorldVector() {
        this(0, 0, 0);
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
    public @NotNull ImmutableWorldVector copyVector() {
        return new ImmutableWorldVector(x, y, z);
    }

    @Override
    @NotNull ImmutableWorldVector operation(double x, double y, double z) {
        return new ImmutableWorldVector(x, y, z);
    }

    @Override
    public String toString() {
        return "ImmutableWorldVector{x=" + x + ", y=" + y + ", z=" + z + "}";
    }

    @Override
    public ImmutableWorldVector asImmutable() {
        return this;
    }
}
