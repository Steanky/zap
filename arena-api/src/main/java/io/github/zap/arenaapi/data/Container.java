package io.github.zap.arenaapi.data;

import java.util.Collection;

/**
 * Generalized interface for a type of class that wraps objects which can be accessed via key or index.
 * @param <K> The type of object used to access the data
 * @param <V> The type of object returned by this object
 */
public interface Container<K, V> extends Iterable<V> {
    /**
     * The name used to uniquely identify this container.
     * @return The unique name of this container
     */
    String name();

    /**
     * The size of the container.
     * @return The number of objects in the container
     */
    int size();

    /**
     * Gets the object, given the provided key.
     * @param value The key to use
     * @return The value, which may be null. May throw IndexOutOfBounds if the container wraps an array or list.
     */
    V get(K value);

    /**
     * Sets the object at the specified index, which must already exist in the case of a list or array.
     * @param key The index or accessor
     * @param value The value to set
     */
    void set(K key, V value);

    /**
     * For list implementations, appends a value to the list. May throw UnsupportedOperationException if the
     * implementation is an array, map, or another kind of collection that does not support appending elements.
     * @param value The value to append
     */
    default void add(V value) { throw new UnsupportedOperationException(); }

    /**
     * Returns a collection containing all of the valid keys for this object.
     */
    Collection<K> keys();

    /**
     * Returns all of the values contained in this Container implementation
     */
    Collection<V> values();
}
