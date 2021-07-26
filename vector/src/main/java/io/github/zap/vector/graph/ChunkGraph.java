package io.github.zap.vector.graph;

import io.github.zap.vector.Vector3I;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a collection of data objects indexed by 3-dimensional integer coordinates.
 */
public interface ChunkGraph<T> extends Iterable<T> {
    @Nullable T elementAt(int x, int y, int z);

    default T elementAt(@NotNull Vector3I vector) {
        return elementAt(vector.x(), vector.y(), vector.z());
    }

    void putElement(int x, int y, int z, @Nullable T element);

    default void putElement(@NotNull Vector3I vector, @Nullable T element) {
        putElement(vector.x(), vector.y(), vector.z(), element);
    }

    boolean removeElement(int x, int y, int z);

    default boolean removeElement(@NotNull Vector3I vector) {
        return removeElement(vector.x(), vector.y(), vector.z());
    }

    boolean hasElementAt(int x, int y, int z);

    default boolean hasElementAt(@NotNull Vector3I vector) {
        return hasElementAt(vector.x(), vector.y(), vector.z());
    }

    int size();
}
