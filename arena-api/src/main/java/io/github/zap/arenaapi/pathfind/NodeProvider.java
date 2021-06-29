package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

/**
 * Implementations of this interface provide PathNode objects. They are used by PathOperations to find out which nodes
 * may be traversed for a given agent, starting at the current node, in the current context.
 *
 * In general, returning fewer nodes is better for memory usage and performance but may result in other problems such
 * as "coarse" paths that appear suboptimal to the user. Returning more nodes may improve path appearance at the cost
 * of performance.
 */
public abstract class NodeProvider {
    private final AversionCalculator aversionCalculator;

    protected NodeProvider(@NotNull AversionCalculator aversionCalculator) {
        this.aversionCalculator = aversionCalculator;
    }

    public abstract void generateNodes(@NotNull PathNode[] buffer, @NotNull PathfinderContext context,
                                       @NotNull PathAgent agent, @NotNull PathNode current);

    public AversionCalculator getAversionCalculator() {
        return aversionCalculator;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof NodeProvider) {
            return obj.getClass() == getClass();
        }

        return false;
    }
}