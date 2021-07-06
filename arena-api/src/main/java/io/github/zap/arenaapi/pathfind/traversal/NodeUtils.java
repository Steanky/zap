package io.github.zap.arenaapi.pathfind.traversal;

import java.util.function.BiConsumer;

public class NodeUtils {
    static <T, V> int setterHelper(T element, T[] array, int index, int currentEmptyCount,
                                   BiConsumer<Integer, V> parentSetter, int parentIndex) {
        if(element != null) {
            if(array[index] == null) {
                array[index] = element;
                currentEmptyCount--;
            }
        }
        else {
            if(array[index] != null) {
                array[index] = null;
                if(++currentEmptyCount == 16) {
                    parentSetter.accept(parentIndex, null);
                }
            }
        }

        return currentEmptyCount;
    }
}
