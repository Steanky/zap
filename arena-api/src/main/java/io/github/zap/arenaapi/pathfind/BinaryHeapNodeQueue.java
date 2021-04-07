package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Implementation of NodeQueue based on a binary min-heap
 */
public class BinaryHeapNodeQueue implements NodeQueue {
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
        PathNode last = nodes[--size];

        nodes[0] = last;
        nodes[size] = null;
        siftDown(0);
        return best;
    }

    @Override
    public void addNode(@NotNull PathNode node) {
        Objects.requireNonNull(node, "node cannot be null!");

        ensureCapacity(size + 1);
        nodes[size] = node;
        siftUp(size++);
    }

    @Override
    public void updateNode(PathNode node) {
        Objects.requireNonNull(node, "node cannot be null!");

        int index = indexOf(node);

        if(index != -1) {
            if(index == 0) {
                siftDown(0);
            }
            else if(index == size - 1) {
                siftUp(index);
            }
            else {
                int parentIndex = (index - 1) >> 1;
                int parentComparison = NodeComparator.instance().compare(node, nodes[parentIndex]);
                if(parentComparison < 0) {
                    siftUp(index);
                }
                else {
                    siftDown(index);
                }
            }
        }
        else {
            addNode(node);
        }
    }

    @Override
    public boolean contains(PathNode node) {
        Objects.requireNonNull(node, "node cannot be null!");

        return indexOf(node) != -1;
    }

    @Override
    public int size() {
        return size;
    }

    private int indexOf(PathNode node) {
        if(size == 0) {
            return -1;
        }

        int index = 0;
        boolean smallerExists = false;
        while(index < size) {
            PathNode sample = nodes[index];
            int currentCompare = NodeComparator.instance().compare(sample, node);

            if(currentCompare == 0) {
                return index;
            }
            else if(currentCompare < 0) { //sample < node
                smallerExists = true;
            }

            /*
             * optimization: if all the nodes we found on this level are larger, it's only going to get bigger and thus
             * we can stop searching
             *0
             * (index & -index) == index is bit magic: it evaluates whether or not index is a power of 2, and thus the
             * last element in a given 'row' of the tree, for which all additional elements are guaranteed to be larger
             * than the smallest value in this row
             */
            if(index != 1 && (index & -index) == index) {
                if(!smallerExists) {
                    return -1;
                }
            }

            index++;
        }

        return -1;
    }

    private void siftUp(int index) {
        while(index > 0) {
            int parentIndex = (index - 1) >> 1;

            PathNode target = nodes[index];
            PathNode parent = nodes[parentIndex];

            if(NodeComparator.instance().compare(parent, target) <= 0) {
                break;
            }

            nodes[parentIndex] = target;
            nodes[index] = parent;

            index = parentIndex;
        }
    }

    private void siftDown(int index) {
        int half = size >> 1;
        while (index < half) {
            int firstIndex = (index << 1) + 1;
            int secondIndex = (index + 1) << 1;
            int smallestIndex;

            PathNode current = nodes[index];
            PathNode first = nodes[firstIndex];
            PathNode second;
            PathNode smallest;

            if(secondIndex < size) {
                second = nodes[secondIndex];

                if(NodeComparator.instance().compare(first, second) <= 0) {
                    smallest = first;
                    smallestIndex = firstIndex;
                }
                else {
                    smallest = second;
                    smallestIndex = secondIndex;
                }
            }
            else {
                smallest = first;
                smallestIndex = firstIndex;
            }

            if(NodeComparator.instance().compare(current, smallest) <= 0) {
                break;
            }

            nodes[index] = smallest;
            nodes[smallestIndex] = current;

            index = smallestIndex;
        }
    }

    private void ensureCapacity(int size) {
        if(size > nodes.length) {
            int newSize = nodes.length << 1;

            if(newSize > Integer.MAX_VALUE - 8) {
                throw new OutOfMemoryError();
            }

            PathNode[] array = new PathNode[newSize];
            System.arraycopy(nodes, 0, array, 0, nodes.length);
            nodes = array;
        }
    }
}