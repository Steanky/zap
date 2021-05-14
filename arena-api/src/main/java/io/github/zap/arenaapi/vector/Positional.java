package io.github.zap.arenaapi.vector;

import org.jetbrains.annotations.NotNull;

/**
 * Describes an object that may have a position; i.e. supplies a VectorAccess object. Positional instances inherit all
 * the methods of VectorAccess, which act on or read from the vector returned by a call to position().
 */
public interface Positional extends VectorAccess {
    /**
     * Returns the VectorAccess object used by the Positional instance to store its location. The VectorAccess may be
     * mutable or immutable, depending on the needs of the Positional implementation.
     * @return The VectorAccess object used
     */
    @NotNull VectorAccess position();

    @Override
    default @NotNull VectorAccess copyVector() {
        return position().copyVector();
    }

    @Override
    default double x() {
        return position().x();
    }

    @Override
    default double y() {
        return position().y();
    }

    @Override
    default double z() {
        return position().z();
    }

    @Override
    default @NotNull VectorAccess add(double x, double y, double z) {
        return position().add(x, y, z);
    }

    @Override
    default @NotNull VectorAccess subtract(double x, double y, double z) {
        return position().subtract(x, y, z);
    }

    @Override
    default @NotNull VectorAccess multiply(double x, double y, double z) {
        return position().multiply(x, y, z);
    }

    @Override
    default @NotNull VectorAccess divide(double x, double y, double z) {
        return position().divide(x, y, z);
    }
}
