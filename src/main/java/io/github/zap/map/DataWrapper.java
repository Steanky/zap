package io.github.zap.map;

/**
 * General interface for a class that wraps data objects so they can be serialized in a unified manner.
 * @param <T> The type of object to wrap, which must be a serializer of itself
 */
public interface DataWrapper<T extends DataSerializer<T>> {
    /**
     * Returns the data object that this instance wraps.
     * @return The underlying data object
     */
    T getDataObject();
}
