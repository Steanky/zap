package io.github.zap.arenaapi.vector;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class WorldVector extends ChunkVector {
    public abstract double worldX();

    public abstract double worldY();

    public abstract double worldZ();

    @Override
    public int chunkX() {
        return blockX() >> 4;
    }

    @Override
    public int chunkZ() {
        return blockZ() >> 4;
    }

    public int blockX() {
        return (int)worldX();
    }

    public int blockY() {
        return (int)worldY();
    }

    public int blockZ() {
        return (int)worldZ();
    }

    public MutableWorldVector asMutable() {
        return new MutableWorldVectorImpl(worldX(), worldY(), worldZ());
    }

    public WorldVector copy() {
        return mutable(worldX(), worldY(), worldZ());
    }

    public double manhattanDistance(double x, double y, double z) {
        return Math.abs(worldX() - x) + Math.abs(worldY() - y) + Math.abs(worldZ() - z);
    }

    public double manhattanDistance(@NotNull WorldVector to) {
        return manhattanDistance(to.worldX(), to.worldY(), to.worldZ());
    }

    public double distanceSquared(double x, double y, double z) {
        double xD = x - worldX();
        double yD = y - worldY();
        double zD = z - worldZ();

        return (xD * xD) + (yD * yD) + (zD * zD);
    }

    public double distanceSquared(@NotNull WorldVector to) {
        return distanceSquared(to.worldX(), to.worldY(), to.worldZ());
    }

    public @NotNull WorldVector add(double x, double y, double z) {
        return immutable(worldX() + x, worldY() + y, worldZ() + z);
    }

    public @NotNull WorldVector add(@NotNull WorldVector other) {
        return add(other.worldX(), other.worldY(), other.worldZ());
    }

    @Override
    public int hashCode() {
        if(!hasHash) {
            hasHash = true;
            hash = Objects.hash(worldX(), worldY(), worldZ());
        }

        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof WorldVector) {
            WorldVector otherVector = (WorldVector) other;
            return otherVector.worldX() == worldX() && otherVector.worldY() == worldY() && otherVector.worldZ() == worldZ();
        }

        return false;
    }

    public static WorldVector immutable(double x, double y, double z) {
        return new WorldVectorImpl(x, y, z);
    }

    public static WorldVector immutable(@NotNull Vector vector) {
        return new WorldVectorImpl(vector.getX(), vector.getY(), vector.getZ());
    }

    public static MutableWorldVector mutable(double x, double y, double z) {
        return new MutableWorldVectorImpl(x, y, z);
    }

    public static MutableWorldVector mutable(@NotNull Vector vector) {
        return new MutableWorldVectorImpl(vector.getX(), vector.getY(), vector.getZ());
    }
}
