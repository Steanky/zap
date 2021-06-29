package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * General implementation for a class managing an aggregation of PathNode objects.
 */
public interface NodeHeap {
    /**
     * Returns, but does not remove, the best (lowest-scoring) node.
     * @return The best node
     */
    @NotNull PathNode peekBest();

    /**
     * Returns and removes the best (lowest-scoring) node.
     * @return The best node
     */
    @NotNull PathNode takeBest();

    /**
     * Adds a node to the heap.
     * @param node The node to add
     */
    void addNode(@NotNull PathNode node);

    /**
     * Performs an update on the given node.
     * @param index The index of the node to update.
     */
    void updateNode(int index);

    /**
     * Check if this NodeHeap contains the given node. This should use equals() comparison rather than reference
     * comparison.
     * @param node The node to test
     * @return Whether or not the node exists
     */
    boolean contains(@NotNull PathNode node);

    /**
     * Returns the index of the given node. This can be used to update specific nodes later.
     * @param node The node to index
     * @return The index of the node
     */
    int indexOf(@NotNull PathNode node);

    @NotNull PathNode nodeAt(int index);

    /**
     * Returns the number of nodes currently in the heap.
     * @return The number of nodes contained in this heap
     */
    int size();

    PathNode[] internalArray();
}
