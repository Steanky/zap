package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

/**
 * Implementation of NodeQueue based on a binary min-heap
 */
public class BinaryHeapNodeQueue implements NodeQueue {
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private static final NodeComparator NODE_COMPARATOR = NodeComparator.instance();

    private PathNode[] nodes;
    private int size = 0;

    public BinaryHeapNodeQueue(int capacity) {
        nodes = new PathNode[capacity];
    }

    @Override
    public @NotNull PathNode peekBest() {
        if(size == 0) {
            throw new IndexOutOfBoundsException();
        }

        return nodes[0];
    }

    @Override
    public @NotNull PathNode takeBest() {
        if(size == 0) {
            throw new IndexOutOfBoundsException();
        }

        PathNode best = nodes[0];
        PathNode last = nodes[(--size)];
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
    public void replaceNode(@NotNull PathNode currentNode, @NotNull PathNode newNode) {
        int index = indexOf(currentNode);

        if(index == -1) {
            addNode(newNode);
            return;
        }

        int comparison = NODE_COMPARATOR.compare(newNode, currentNode);
        if(comparison < 0) {
            siftUp(index, newNode);
        }
        else if(comparison > 0) {
            siftDown(index, newNode);
        }
        else {
            nodes[index] = newNode;
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
        if(size > nodes.length) {
            int newSize = nodes.length << 1;

            if(newSize > MAX_ARRAY_SIZE) {
                throw new OutOfMemoryError();
            }

            PathNode[] array = new PathNode[newSize];
            System.arraycopy(nodes, 0, array, 0, this.size);
            nodes = array;
        }
    }
}