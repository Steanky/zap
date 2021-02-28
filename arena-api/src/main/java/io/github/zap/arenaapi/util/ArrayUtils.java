package io.github.zap.arenaapi.util;

import java.lang.reflect.Array;

public final class ArrayUtils {
    /**
     * Combines any number of similarly typed arrays into a single array of the same type. Uses System.arraycopy.
     * @param arrays The arrays to combine
     * @param <T> The type of the array
     * @return An array containing all the specified arrays, whose elements are ordered in the same way as they
     * are provided
     */
    @SafeVarargs
    public static <T> T[] combine(T[]... arrays) {
        int size = 0;

        for(T[] array : arrays) {
            size += array.length;
        }

        //first call to getComponentType returns a type that is itself an array, so we call twice
        Object composite = Array.newInstance(arrays.getClass().getComponentType().getComponentType(), size);

        int destinationOffset = 0;
        for (T[] sample : arrays) {
            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(sample, 0, composite, destinationOffset, sample.length);
            destinationOffset += sample.length;
        }

        //noinspection unchecked
        return (T[])composite;
    }

    public static <T> T[] combine(Iterable<T[]> arrays, Class<T> elementType) {
        int size = 0;

        for(T[] array : arrays) {
            size += array.length;
        }

        Object composite = Array.newInstance(elementType, size);
        int destinationOffset = 0;
        for (T[] sample : arrays) {
            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(sample, 0, composite, destinationOffset, sample.length);
            destinationOffset += sample.length;
        }

        //noinspection unchecked
        return (T[])composite;
    }
}
