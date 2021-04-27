package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * General implementation for a class managing an aggregation of nodes.
 */
public interface NodeHeap {
    @NotNull PathNode peekBest();

    @NotNull PathNode takeBest();

    @NotNull PathNode nodeAt(int index);

    void addNode(@NotNull PathNode node);

    void updateNode(@NotNull PathNode currentNode, Consumer<PathNode> updateFunction);

    boolean contains(@NotNull PathNode node);

    int size();
}
