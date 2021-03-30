package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.PriorityQueue;

class NodeQueue {
    private static final NodeComparator comparator = NodeComparator.instance();

    private int size;
    private PathNode[] array;

    NodeQueue(int initialCapacity) {
        array = new PathNode[initialCapacity];
        size = 0;
    }

    public void addNode(@NotNull PathNode node) {
        ensureCapacity();
        siftUp(++size, node);
    }

    public PathNode peekFirst() {
        return array[0];
    }

    public PathNode removeFirst() {
        PathNode first = array[0];
        removeAt(0);
        return first;
    }

    public void updateNode(PathNode node) {

    }

    public int size() {
        return size;
    }

    private void ensureCapacity() {
        if(size + 1 > array.length) {
            int newLength = array.length + ((array.length < 64) ? (array.length + 2) : (array.length / 2));
            array = Arrays.copyOf(array, newLength);
        }
    }

    private void siftUp(int indexAt, PathNode node) {
        while (indexAt > 0) {
            int parentIndex = (indexAt - 1) / 2;
            PathNode parentNode = array[parentIndex];
            if (comparator.compare(node, parentNode) > 0) {
                break;
            }
            array[indexAt] = parentNode;
            indexAt = parentIndex;
        }

        array[indexAt] = node;
    }

    private void siftDown(int index, PathNode node) {
        int half = size / 2;
        while (index < half) {
            int child = (index * 2) + 1;
            PathNode childNode = array[child];
            int right = child + 1;
            if (right < size && comparator.compare(childNode, array[right]) > 0) {
                childNode = array[child = right];
            }

            if (comparator.compare(childNode, node) <= 0) {
                break;
            }

            array[index] = childNode;
            index = child;
        }
        array[index] = node;
    }

    private void removeAt(int index) {
        --size;
        if (size == index)
            array[index] = null;
        else {
            PathNode moved = array[size];
            array[size] = null;
            siftDown(index, moved);
            if (array[index] == moved) {
                siftUp(index, moved);
            }
        }
    }
}
