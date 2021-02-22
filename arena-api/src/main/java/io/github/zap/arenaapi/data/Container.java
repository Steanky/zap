package io.github.zap.arenaapi.data;

import java.util.Collection;

/**
 * Generalized interface for a type of class that wraps objects which can be accessed via key or index.
 * @param <K> The type of object used to access the data
 * @param <V> The type of object returned by this object
 */
public interface Container<K, V> extends Iterable<V> {
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
     * Appends the value to the underlying collection. May throw UnsupportedOperationException for some implementations.
     * @param value The value to append
     */
    void add(V value);

    /**
     * Removes a value from the container. If the implementation is fixed-size, calling remove() must return true if
     * an object was removed and set the value in the backing object to default. If the container is read-only, this
     * may throw an UnsupportedOperationException.
     * @param key The object to remove
     * @return True if something as removed
     */
    boolean remove(K key);

    /**
     * Returns whether or not this Container implementation is fixed-size. Fixed-size containers must always return
     * the same value for size (and iterate the same number of elements).
     * @return True if this Container is fixed-size, false otherwise
     */
    boolean fixedSize();

    /**
     * Returns whether or not the Container is read-only. Read-only containers will throw exceptions whenever a 'write'
     * operation is attempted (set or remove). keys() and values() will return copies of the backing object.
     * @return True if this Container is read-only, false otherwise
     */
    boolean readOnly();

    /**
     * Whether or not the Container allows indexing (accessing values by keys).
     * @return True if this Container is indexable, false otherwise
     */
    boolean supportsIndexing();

    /**
     * Whether or not we can append values to this container (if not, add() will throw an exception).
     * @return Whether or not add() will work
     */
    boolean canAppend();

    /**
     * Returns a collection containing all of the valid keys for this object. This collection may be write-through in
     * certain cases, ex. a Map.
     */
    Collection<K> keys();

    /**
     * Returns all of the values contained in this Container implementation. This collection must always be
     * write-through; that is, changes made to the collection will be reflected in the underlying collection wrapped
     * by this container.
     */
    Collection<V> values();

    /**
     * Returns the type of object used to access values.
     */
    Class<K> keyClass();

    /**
     * Returns the type of values contained in this Container.
     */
    Class<V> valueClass();
}
