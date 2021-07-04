package io.github.zap.vector;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a 3-dimensional coordinate, along with useful methods for manipulating it. Implementations may be mutable or
 * immutable; which will determine the behavior of basic vector arithmetic (mutable vectors will act on themselves, and
 * immutable vectors will return new instances without changing their own internal state).
 */
public interface VectorAccess extends ChunkVectorAccess {
    double EPSILON = 1.0E-6D;

    VectorAccess UNIT = new ImmutableWorldVector(1, 1, 1);
    VectorAccess ZERO = new ImmutableWorldVector(0, 0, 0);

    double x();

    double y();

    double z();

    @NotNull VectorAccess add(double x, double y, double z);

    default @NotNull VectorAccess add(@NotNull VectorAccess other) {
        return add(other.x(), other.y(), other.z());
    }

    @NotNull VectorAccess subtract(double x, double y, double z);

    default @NotNull VectorAccess subtract(@NotNull VectorAccess other) {
        return subtract(other.x(), other.y(), other.z());
    }

    @NotNull VectorAccess multiply(double x, double y, double z);

    default @NotNull VectorAccess multiply(@NotNull VectorAccess other) {
        return multiply(other.x(), other.y(), other.z());
    }

    default @NotNull VectorAccess multiply(double v) {
        return multiply(v, v, v);
    }

    @NotNull VectorAccess divide(double x, double y, double z);

    default @NotNull VectorAccess divide(@NotNull VectorAccess other) {
        return divide(other.x(), other.y(), other.z());
    }

    @NotNull VectorAccess copyVector();

    @Override
    default int chunkX() {
        return blockX() >> 4;
    }

    @Override
    default int chunkZ() {
        return blockZ() >> 4;
    }

    default int blockX() {
        return (int) x();
    }

    default int blockY() {
        return (int) y();
    }

    default int blockZ() {
        return (int) z();
    }

    default double manhattanDistance(double x, double y, double z) {
        return Math.abs(x() - x) + Math.abs(y() - y) + Math.abs(z() - z);
    }

    default double manhattanDistance(@NotNull VectorAccess to) {
        return manhattanDistance(to.x(), to.y(), to.z());
    }

    default double distanceSquared(double x, double y, double z) {
        double xD = x() - x;
        double yD = y() - y;
        double zD = z() - z;

        return (xD * xD) + (yD * yD) + (zD * zD);
    }

    default double distanceSquared(@NotNull VectorAccess to) {
        return distanceSquared(to.x(), to.y(), to.z());
    }

    default double distance(double x, double y, double z) {
        return Math.sqrt(distanceSquared(x, y, z));
    }

    default double distance(@NotNull VectorAccess to) {
        return distance(to.x(), to.y(), to.z());
    }

    default double magnitude() {
        return distance(0, 0, 0);
    }

    default double magnitudeSquared() {
        return distanceSquared(0, 0, 0);
    }

    default ImmutableWorldVector asImmutable() {
        return new ImmutableWorldVector(x(), y(), z());
    }

    default MutableWorldVector asMutable() {
        return new MutableWorldVector(x(), y(), z());
    }

    default @NotNull Vector asBukkit() {
        return new Vector(x(), y(), z());
    }

    default @NotNull VectorAccess asChunkRelative() {
        return VectorAccess.immutable(blockX() & 15, blockY(), blockZ() & 15);
    }

    static @NotNull ImmutableWorldVector immutable(double x, double y, double z) {
        return new ImmutableWorldVector(x, y, z);
    }

    static @NotNull ImmutableWorldVector immutable(@NotNull Vector vector) {
        return new ImmutableWorldVector(vector.getX(), vector.getY(), vector.getZ());
    }

    default @NotNull ImmutableWorldVector toBlockVector() {
        return new ImmutableWorldVector(blockX(), blockY(), blockZ());
    }

    static @NotNull MutableWorldVector mutable(double x, double y, double z) {
        return new MutableWorldVector(x, y, z);
    }

    static @NotNull MutableWorldVector mutable(@NotNull Vector vector) {
        return new MutableWorldVector(vector.getX(), vector.getY(), vector.getZ());
    }

    default boolean validChunkVector() {
        return blockX() >= 0 && blockX() < 16 && blockY() >= 0 && blockY() < 256 && blockZ() >= 0 && blockZ() < 16;
    }
}
