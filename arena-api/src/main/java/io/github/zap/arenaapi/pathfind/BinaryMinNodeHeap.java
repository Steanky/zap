package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
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

        return best;
    }

    @Override
    public void addNode(@NotNull PathNode node) {
        ensureCapacity(size + 1);
        siftUp(size++, node);
    }

    @Override
    public void updateNode(@NotNull PathNode currentNode, @NotNull Consumer<PathNode> updateFunction) {
        int index = indexOf(currentNode);

        if(index == -1) {
            addNode(currentNode);
            return;
        }

        Score scoreBefore = currentNode.score;
        updateFunction.accept(currentNode);
        Score scoreAfter = currentNode.score;

        int comparison = ScoreComparator.instance().compare(scoreAfter, scoreBefore);
        if(comparison < 0) {
            siftUp(index, currentNode);
        }
        else if(comparison > 0) {
            siftDown(index, currentNode);
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

    private int indexOf(PathNode node) {
        for(int i = 0; i < size; i++) {
            if(nodes[i].equals(node)) {
                return i;
            }
        }

        return -1;
    }

    private void siftUp(int index, PathNode node) {
        while(index > 0) {
            int parentIndex = (index - 1) >> 1;

            PathNode parent = nodes[parentIndex];

            if(NODE_COMPARATOR.compare(node, parent) >= 0) {
                break;
            }

            nodes[index] = parent;
            index = parentIndex;
        }

        nodes[index] = node;
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
            index = childIndex;
        }

        nodes[index] = node;
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