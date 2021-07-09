package io.github.zap.vector.graph;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a collection of data objects indexed by 3-dimensional integer coordinates.
 */
public interface ChunkGraph<T> {
    @Nullable T elementAt(int x, int y, int z);

    void putElement(int x, int y, int z, @Nullable T element);

    boolean removeElement(int x, int y, int z);

    boolean hasElement(int x, int y, int z);
}
