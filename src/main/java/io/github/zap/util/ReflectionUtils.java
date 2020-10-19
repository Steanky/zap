package io.github.zap.util;

import java.lang.reflect.Array;
import java.util.ArrayList;

public final class ReflectionUtils {
    public static Class<?> getUnderlyingComponentType(Class<?> clazz) {
        Class<?> componentType = clazz.getComponentType(); //get the underlying component type of this array, no matter the dimension

        while(componentType != null) {
            Class<?> next = componentType.getComponentType();

            if(next == null) {
                return componentType;
            }

            componentType = next;
        }

        return null;
    }

    public static int[] getDimensionLengths(Object array) {
        int dimensions = 1 + array.getClass().getName().lastIndexOf('[');
        int[] lengths = new int[dimensions];

        Object dimension = array;
        for(int i = 0; i < dimensions; i++) {
            int length = Array.getLength(dimension);
            lengths[i] = length;

            if(length > 0) {
                dimension = Array.get(dimension, 0);
            }
        }

        return lengths;
    }
}
