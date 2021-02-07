package io.github.zap.arenaapi;

import java.util.Iterator;

/**
 * An iterator that reports its length. Necessary if you want to pre-allocate an array to hold all the values,
 * possibly before they are created.
 * @param <T> The type of object this iterator iterates
 */
public interface BoundedIterator<T> extends Iterator<T> {
    /**
     * Returns the number of elements this iterator will iterate.
     * @return The number of elements in this iterator
     */
    int getLength();
}
