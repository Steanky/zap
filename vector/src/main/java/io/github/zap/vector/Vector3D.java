package io.github.zap.vector;

/**
 * General interface for a class encapsulating three double values. Implementations of this interface are required to
 * be effectively immutable; ex. the returned value of x(), y(), and z() cannot change for the lifetime of the object.
 */
public interface Vector3D {
    double x();

    double y();

    double z();
}
