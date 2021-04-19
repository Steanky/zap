package io.github.zap.arenaapi.vector2;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class MutableBlockVector extends BlockVector {
    abstract void setBlockX(int x);

    abstract void setBlockY(int y);

    abstract void setBlockZ(int z);

    @Override
    public @NotNull MutableBlockVector sum(@NotNull BlockVector other) {
        setBlockX(blockX() + other.blockX());
        setBlockY(blockY() + other.blockY());
        setBlockZ(blockZ() + other.blockZ());
        return this;
    }

    @Override
    public @NotNull MutableBlockVector product(@NotNull BlockVector other) {
        setBlockX(blockX() * other.blockX());
        setBlockY(blockY() * other.blockY());
        setBlockZ(blockZ() * other.blockZ());
        return this;
    }

    @Override
    public @NotNull BlockVector quotient(@NotNull BlockVector divideBy) {
        setBlockX(blockX() / divideBy.blockX());
        setBlockY(blockY() / divideBy.blockY());
        setBlockZ(blockZ() / divideBy.blockZ());
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockX(), blockY(), blockZ());
    }
}
