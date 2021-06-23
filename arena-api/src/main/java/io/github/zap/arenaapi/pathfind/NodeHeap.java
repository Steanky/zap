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
     * Performs an update on the given node. For this to work, the node must be part of the collection.
     * @param currentNode The node to update
     * @param updateConsumer The consumer that will be used to update the node
     */
    void updateNode(@NotNull PathNode currentNode, Consumer<PathNode> updateConsumer);

    /**
     * Check if this NodeHeap contains the given node. This should use equals() comparison rather than reference
     * comparison.
     * @param node The node to test
     * @return Whether or not the node exists
     */
    boolean contains(@NotNull PathNode node);

    /**
     * Returns the number of nodes currently in the heap.
     * @return The number of nodes contained in this heap
     */
    int size();
}
