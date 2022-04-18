package io.github.zap.vector.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

class NodeUtils {
    static <T, V> int setterHelper(@Nullable T element, T[] array, int index, int currentEmptyCount,
                                   @NotNull BiConsumer<Integer, V> parentSetter, int parentIndex) {
        if(element != null) {
            if(array[index] == null) {
                array[index] = element;
                currentEmptyCount--;
            }
        }
        else {
            if(array[index] != null) {
                array[index] = null;
                currentEmptyCount++;
            }
        }

        //remove current node (whatever it is) from the parent, if we're totally empty
        if(currentEmptyCount == 16) {
            parentSetter.accept(parentIndex, null);
        }

        return currentEmptyCount;
    }
}
