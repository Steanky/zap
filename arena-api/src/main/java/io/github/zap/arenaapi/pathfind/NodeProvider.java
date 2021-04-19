package io.github.zap.arenaapi.pathfind;

import org.bukkit.block.data.BlockData;
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
    /**
     * NodeProvider that can be used to debug pathfinding implementations. It only allows movement along the same
     * Y-axis, and will not consider nodes that move into non-air blocks.
     */
    public static final NodeProvider DEBUG = new NodeProvider() {
        @Override
        public @NotNull PathNode[] generateNodes(@NotNull PathfinderContext context, @NotNull PathNode from) {
            PathNode[] nodes = new PathNode[4];

            nodes[0] = from.add(Direction.NORTH.offset());
            nodes[1] = from.add(Direction.EAST.offset());
            nodes[2] = from.add(Direction.SOUTH.offset());
            nodes[3] = from.add(Direction.WEST.offset());

            return nodes;
        }

        @Override
        public boolean mayTraverse(@NotNull PathfinderContext context, @NotNull PathAgent agent,
                                   @NotNull PathNode start, @NotNull PathNode next) {
            BlockData blockData = context.blockProvider().getData((int)next.x, (int)next.y, (int)next.z);
            return blockData != null && blockData.getMaterial().isAir();
        }
    };

    /**
     * NodeProvider implementation that mimics vanilla pathfinding to a certain extent, with some bugs fixed. Uses
     * advanced collision detection and supports asynchronous PathfinderEngine implementations.
     */
    public static final NodeProvider DEFAULT = new NodeProvider() {
        @Override
        public @NotNull PathNode[] generateNodes(@NotNull PathfinderContext context, @NotNull PathNode nodeAt) {
            PathNode[] nodes = new PathNode[8];
            return nodes;
        }

        @Override
        public boolean mayTraverse(@NotNull PathfinderContext context, @NotNull PathAgent agent,
                                   @NotNull PathNode start, @NotNull PathNode next) {
            return false;
        }
    };

    /**
     * Generates an array of PathNode objects representing the points that the given PathAgent might be able to traverse
     * from a given 'origin' node. PathNode instances may be constructed from the origin node by using the origin's
     * add() or link() methods. Nodes generated this way will have their parent node set to the origin node.
     *
     * The returned array may contain nodes that can't be traversed.
     * @param context The current PathfinderContext
     * @param nodeAt The PathNode we're pathfinding from
     * @return A null-terminated array of PathNode objects, which may be empty or contain null elements
     */
    public abstract @NotNull PathNode[] generateNodes(@NotNull PathfinderContext context, @NotNull PathNode nodeAt);

    /**
     * Returns true if PathAgent 'agent' may traverse the distance between the PathNode 'start' and PathNode 'next'.
     * Only nodes for which this returns true will be inspected by a PathOperation.
     * @param context The current PathfinderContext
     * @param agent The current PathAgent
     * @param start The starting node
     * @param next The ending node
     * @return Whether or not the agent may traverse to 'next' from 'start'
     */
    public abstract boolean mayTraverse(@NotNull PathfinderContext context, @NotNull PathAgent agent,
                                        @NotNull PathNode start, @NotNull PathNode next);

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