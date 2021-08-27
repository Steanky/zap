package io.github.zap.arenaapi.pathfind.operation;

import io.github.zap.vector.Vector3I;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * General implementation for a class managing an aggregation of PathNode objects.
 */
interface NodeHeap {
    /**
     * Returns, but does not remove, the best (lowest-scoring) node.
     * @return The best node
     */
    @NotNull PathNodeImpl peekBest();

    /**
     * Returns and removes the best (lowest-scoring) node.
     * @return The best node
     */
    @NotNull PathNodeImpl takeBest();

    /**
     * Adds a node to the heap.
     * @param node The node to add
     */
    void addNode(@NotNull PathNodeImpl node);

    /**
     * Performs an update on the given node.
     * @param index The index of the node to update.
     */
    void replaceNode(int index, @NotNull PathNodeImpl replace);

    /**
     * Check if this NodeHeap contains the given node. This should use equals() comparison rather than reference
     * comparison.
     * @param node The node to test
     * @return Whether or not the node exists
     */
    boolean contains(@NotNull PathNodeImpl node);

    /**
     * Returns the index of the given node. This can be used to update specific nodes later.
     * @param node The node to index
     * @return The index of the node
     */
    int indexOf(@NotNull PathNodeImpl node);

    @NotNull PathNodeImpl nodeAt(int index);

    @Nullable PathNodeImpl nodeAt(int x, int y, int z);

    default @Nullable PathNodeImpl nodeAt(@NotNull Vector3I vector) {
        return nodeAt(vector.x(), vector.y(), vector.z());
    }

    /**
     * Returns the number of nodes currently in the heap.
     * @return The number of nodes contained in this heap
     */
    int size();

    boolean isEmpty();

    @NotNull PathNodeImpl[] internalArray();

    @NotNull List<PathNodeImpl> toSortedList();
}
