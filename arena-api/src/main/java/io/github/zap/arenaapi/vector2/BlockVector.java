package io.github.zap.arenaapi.vector2;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class BlockVector extends ChunkVector {
    public abstract int blockX();

    public abstract int blockY();

    public abstract int blockZ();

    @Override
    int chunkX() {
        return blockX() >> 4;
    }

    @Override
    int chunkZ() {
        return blockZ() >> 4;
    }

    public @NotNull BlockVector sum(@NotNull BlockVector other) {
        return immutable(blockX() + other.blockX(), blockY() + other.blockY(), blockZ() + other.blockZ());
    }

    public @NotNull BlockVector product(@NotNull BlockVector other) {
        return immutable(blockX() * other.blockX(), blockY() * other.blockY(), blockZ() * other.blockZ());
    }

    public @NotNull BlockVector quotient(@NotNull BlockVector divideBy) {
        return immutable(blockX() / divideBy.blockX(), blockY() / divideBy.blockY(), blockZ() / divideBy.blockZ());
    }

    public @NotNull BlockVector chunkRelative() {
        return immutable(blockX() & 15, blockY(), blockZ() & 15);
    }

    public @NotNull MutableBlockVector toMutable() {
        return mutable(blockX(), blockY(), blockZ());
    }

    public int manhattanDistance(int x, int y, int z) {
        return Math.abs(blockX() - x) + Math.abs(blockY() - y) + Math.abs(blockZ() - z);
    }

    public int manhattanDistance(@NotNull BlockVector to) {
        return manhattanDistance(to.blockX(), to.blockY(), to.blockZ());
    }

    public double distanceSquared(int x, int y, int z) {
        double xD = x - blockX();
        double yD = y - blockY();
        double zD = z - blockZ();

        return (xD * xD) + (yD * yD) + (zD * zD);
    }

    public double distanceSquared(@NotNull BlockVector to) {
        return distanceSquared(to.blockX(), to.blockY(), to.blockZ());
    }

    @Override
    public int hashCode() {
        if(!hasHash) {
            hasHash = true;
            hash = Objects.hash(blockX(), blockY(), blockZ());
        }

        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof BlockVector) {
            BlockVector otherVector = (BlockVector) other;
            return otherVector.blockX() == blockX() && otherVector.blockY() == blockY() && otherVector.blockZ() == blockZ();
        }

        return false;
    }

    public static BlockVector immutable(int x, int y, int z) {
        return new BlockVectorImpl(x, y, z);
    }

    public static MutableBlockVector mutable(int x, int y, int z) {
        return new MutableBlockVectorImpl(x, y, z);
    }
}