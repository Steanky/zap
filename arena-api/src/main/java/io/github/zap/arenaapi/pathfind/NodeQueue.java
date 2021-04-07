package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

/**
 * General implementation for a class managing an aggregation of nodes.
 */
public interface NodeQueue {
    @NotNull PathNode peekBest();

    @NotNull PathNode takeBest();

    void addNode(PathNode node);

    void updateNode(PathNode node);

    boolean contains(PathNode node);

    int size();
}
