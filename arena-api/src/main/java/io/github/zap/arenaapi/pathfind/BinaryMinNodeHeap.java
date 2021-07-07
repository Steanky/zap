package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.pathfind.traversal.NodeGraph;
import io.github.zap.arenaapi.pathfind.traversal.NodeLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Implementation of NodeQueue based on a binary min-heap
 */
class BinaryMinNodeHeap implements NodeHeap {
    private static final int DEFAULT_CAPACITY = 16;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private static final NodeComparator NODE_COMPARATOR = NodeComparator.instance();

    private PathNode[] nodes;
    private int size = 0;

    BinaryMinNodeHeap(int initialCapacity) {
        nodes = new PathNode[initialCapacity];
    }

    BinaryMinNodeHeap() {
        this(DEFAULT_CAPACITY);
    }

    @Override
    public @NotNull PathNode peekBest() {
        return nodes[0];
    }

    @Override
    public @NotNull PathNode takeBest() {
        PathNode best = nodes[0];
        PathNode last = nodes[--size];
        nodes[size] = null;

        if(size > 0) {
            siftDown(0, last);
        }

        best.heapIndex = -1;
        return best;
    }

    @Override
    public void addNode(@NotNull PathNode node) {
        ensureCapacity(size + 1);
        siftUp(size++, node);
    }

    @Override
    public void replaceNode(int index, @NotNull PathNode replace) {
        PathNode currentNode = nodes[index];

        int comparison = NODE_COMPARATOR.compare(currentNode, replace);
        if(comparison < 0) {
            siftUp(index, replace);
        }
        else if(comparison > 0) {
            siftDown(index, replace);
        }
        else {
            nodes[index] = replace;
            replace.heapIndex = index;
        }
    }

    @Override
    public boolean contains(@NotNull PathNode node) {
        return indexOf(node) > -1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public PathNode[] internalArray() {
        return nodes;
    }

    @Override
    public @NotNull List<PathNode> toSortedList() {
        List<PathNode> nodes = new ArrayList<>();

        while(size > 0) {
            nodes.add(takeBest());
        }

        return nodes;
    }

    public int indexOf(@NotNull PathNode node) {
        return node.heapIndex;
    }

    @Override
    public @NotNull PathNode nodeAt(int index) {
        if(index < size) {
            return nodes[index];
        }

        throw new IndexOutOfBoundsException();
    }

    @Override
    public @Nullable PathNode nodeAt(int x, int y, int z) {
        for(int i = 0; i < size; i++) {
            PathNode node = nodes[i];
            if(node.positionEquals(x, y, z)) {
                return node;
            }
        }

        return null;
    }

    private void siftUp(int index, PathNode node) {
        while(index > 0) {
            int parentIndex = (index - 1) >> 1;

            PathNode parent = nodes[parentIndex];

            if(NODE_COMPARATOR.compare(node, parent) >= 0) {
                break;
            }

            nodes[index] = parent;
            parent.heapIndex = index;

            index = parentIndex;
        }

        nodes[index] = node;
        node.heapIndex = index;
    }

    private void siftDown(int index, PathNode node) {
        int half = size >> 1;
        while (index < half) {
            int childIndex = (index << 1) + 1;
            PathNode smallestChild = nodes[childIndex];
            int secondChild = childIndex + 1;

            if(secondChild < size && NODE_COMPARATOR.compare(smallestChild, nodes[secondChild]) > 0) {
                smallestChild = nodes[childIndex = secondChild];
            }

            if(NODE_COMPARATOR.compare(node, smallestChild) <= 0) {
                break;
            }

            nodes[index] = smallestChild;
            smallestChild.heapIndex = index;
            index = childIndex;
        }

        nodes[index] = node;
        node.heapIndex = index;
    }

    private void ensureCapacity(int size) {
        int length = nodes.length;
        if(size > length) {
            int newCapacity = length + (length < 64 ? length + 2 : length >> 1);

            if (newCapacity - MAX_ARRAY_SIZE > 0)
                newCapacity = hugeCapacity(size);

            nodes = Arrays.copyOf(nodes, newCapacity);
        }
    }

    private int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) {
            throw new OutOfMemoryError();
        }

        return minCapacity > MAX_ARRAY_SIZE ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }
}