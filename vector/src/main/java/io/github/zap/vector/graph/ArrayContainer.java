package io.github.zap.vector.graph;

import org.jetbrains.annotations.NotNull;

public class ArrayContainer<T> {
    public record Entry<T>(T element, int index) {}

    protected final T[] array;

    public ArrayContainer(@NotNull T[] array) {
        this.array = array;
    }

    public @NotNull Entry<T> firstNonNull(int start) {
        for(int i = start; i < array.length; i++) {
            T element = array[i];

            if(element != null) {
                return new Entry<>(element, i);
            }
        }

        throw new IllegalStateException("No more non-null elements");
    }

    public boolean hasNonNull(int start) {
        for(int i = start; i < array.length; i++) {
            T element = array[i];

            if(element != null) {
                return true;
            }
        }

        return false;
    }

    public boolean inRange(int index) {
        return index < array.length && index > -1;
    }
}
