package io.github.zap.arenaapi.vector;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class MutableWorldVector extends WorldVector {
    public abstract void setWorldX(double x);

    public abstract void setWorldY(double y);

    public abstract void setWorldZ(double z);

    @Override
    public @NotNull MutableWorldVector add(double x, double y, double z) {
        setWorldX(worldX() + x);
        setWorldY(worldY() + y);
        setWorldZ(worldZ() + z);
        return this;
    }

    @Override
    public @NotNull MutableWorldVector add(@NotNull WorldVector other) {
        return add(other.worldX(), other.worldY(), other.worldZ());
    }

    @Override
    public int hashCode() {
        return hash = Objects.hash(worldX(), worldY(), worldZ());
    }
}
