package io.github.zap.vector;

import org.jetbrains.annotations.NotNull;

/**
 * Class used internally to define common operations for all vectors.
 */
abstract class WorldVector<T extends WorldVector<T>> implements VectorAccess {
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (int)(Double.doubleToLongBits(this.x()) ^ Double.doubleToLongBits(this.x()) >>> 32);
        hash = 79 * hash + (int)(Double.doubleToLongBits(this.y()) ^ Double.doubleToLongBits(this.y()) >>> 32);
        hash = 79 * hash + (int)(Double.doubleToLongBits(this.z()) ^ Double.doubleToLongBits(this.z()) >>> 32);
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof WorldVector) {
            WorldVector<?> otherVector = (WorldVector<?>) other;

            return Math.abs(otherVector.x() - x()) < EPSILON &&
                    Math.abs(otherVector.y() - y()) < EPSILON &&
                    Math.abs(otherVector.z() - z()) < EPSILON;
        }

        return false;
    }

    @Override
    public String toString() {
        return "WorldVector{x=" + x() + ", y=" + y() + ", z=" + z() + "}";
    }

    public @NotNull T add(double x, double y, double z) {
        return operation(x() + x, y() + y, z() + z);
    }

    public @NotNull T subtract(double x, double y, double z) {
        return operation(x() - x, y() - y, z() - z);
    }

    public @NotNull T multiply(double x, double y, double z) {
        return operation(x() * x, y() * y, z() * z);
    }

    public @NotNull T divide(double x, double y, double z) {
        return operation(x() / x, y() / y, z() / z);
    }

    abstract @NotNull T operation(double x, double y, double z);
}
