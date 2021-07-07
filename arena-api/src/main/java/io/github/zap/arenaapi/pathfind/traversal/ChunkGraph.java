package io.github.zap.arenaapi.pathfind.traversal;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a collection of data objects indexed by 3-dimensional integer coordinates.
 */
public interface ChunkGraph<T> {
    @Nullable T elementAt(int x, int y, int z);

    void putElement(int x, int y, int z, @Nullable T element);

    void removeElement(int x, int y, int z);

    boolean hasElement(int x, int y, int z);
}
