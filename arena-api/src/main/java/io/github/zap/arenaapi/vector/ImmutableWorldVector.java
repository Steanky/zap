package io.github.zap.arenaapi.vector;

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
    @NotNull
    protected ImmutableWorldVector operation(double x, double y, double z) {
        return new ImmutableWorldVector(x, y, z);
    }
}
