package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

/**
 * General implementation for a class managing an aggregation of nodes.
 */
public interface NodeQueue {
    @NotNull PathNode peekBest();

    @NotNull PathNode takeBest();

    void addNode(@NotNull PathNode node);

    void replaceNode(@NotNull PathNode currentNode, @NotNull PathNode newNode);

    boolean contains(@NotNull PathNode node);

    int size();
}
