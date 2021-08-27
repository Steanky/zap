package io.github.zap.arenaapi.pathfind.step;

import io.github.zap.arenaapi.pathfind.agent.PathAgent;
import io.github.zap.arenaapi.pathfind.path.PathNode;
import io.github.zap.arenaapi.pathfind.path.PathNodeFactory;
import io.github.zap.arenaapi.pathfind.context.PathfinderContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementations of this interface provide PathNode objects. They are used by PathOperations to find out which nodes
 * may be traversed for a given agent, starting at the current node, in the current context.
 *
 * In general, returning fewer nodes is better for memory usage and performance but may result in other problems such
 * as "coarse" paths that appear suboptimal to the user. Returning more nodes may improve path appearance at the cost
 * of performance.
 */
public interface NodeExplorer {
    <T extends PathNode> void exploreNodes(@NotNull PathfinderContext context, @NotNull PathAgent agent, @Nullable T[] buffer,
                                           @NotNull T current, @NotNull PathNodeFactory<T> pathNodeFactory);
}