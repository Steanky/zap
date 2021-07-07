package io.github.zap.arenaapi.pathfind.traversal;

import io.github.zap.arenaapi.pathfind.PathNode;
import io.github.zap.arenaapi.pathfind.PathOperation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used to store nodes in an easily accessible way that corresponds to their in-world coordinates.
 */
public interface NodeGraph {
    @Nullable NodeLocation nodeAt(int x, int y, int z);

    void putNode(int x, int y, int z, @NotNull PathNode node, @NotNull PathOperation operation);

    void removeNode(int x, int y, int z);

    void removeChunk(int x, int z);

    boolean containsNode(int x, int y, int z);
}
