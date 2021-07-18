package io.github.zap.vector;

/**
 * General interface for a class encapsulating two integer values. Implementations of this interface are required to
 * be effectively immutable; ex. the returned value of x() and z() cannot change for the lifetime of the object.
 */
public interface Vector2I {
    int x();

    int z();
}
