package io.github.zap.arenaapi.util;

import java.lang.reflect.Array;

public final class ArrayUtils {
    @SafeVarargs
    public static <T> T[] combine(T[]... arrays) {
        int size = 0;

        for(T[] array : arrays) {
            size += array.length;
        }

        //first call to ComponentType returns a type that is itself an array, so we call twice
        Object composite = Array.newInstance(arrays.getClass().getComponentType().getComponentType(), size);

        int destinationOffset = 0;
        for (T[] sample : arrays) {
            //copy over using optimized arraycopy
            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(sample, 0, composite, destinationOffset, sample.length);
            destinationOffset += sample.length;
        }

        //noinspection unchecked
        return (T[])composite;
    }
}
