package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.MutableWorldVector;
import io.github.zap.nms.common.world.BlockCollisionSnapshot;
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
     * NodeProvider that can be used to debug pathfinding implementations. It will only generate nodes along the same
     * Y-axis, and will not consider nodes that move into non-air blocks. Agents will effectively move according to
     * taxicab geometry.
     */
    public static final NodeProvider DEBUG = new NodeProvider() {
        @Override
        public @NotNull PathNode[] generateNodes(@NotNull PathfinderContext context, @NotNull PathNode from) {
            PathNode[] nodes = new PathNode[4];

            nodes[0] = from.chain(Direction.NORTH);
            nodes[1] = from.chain(Direction.EAST);
            nodes[2] = from.chain(Direction.SOUTH);
            nodes[3] = from.chain(Direction.WEST);

            return nodes;
        }

        @Override
        public boolean mayTraverse(@NotNull PathfinderContext context, @NotNull PathAgent agent,
                                   @NotNull PathNode start, @NotNull PathNode next) {
            BlockCollisionSnapshot blockData = context.blockProvider().getBlock(next.blockX(), next.blockY(), next.blockZ());
            return blockData != null && blockData.data().getMaterial().isAir();
        }
    };

    /**
     * NodeProvider implementation that mimics vanilla pathfinding to a certain extent, with some bugs fixed. Uses
     * advanced collision detection and supports asynchronous PathfinderEngine implementations.
     */
    public static final NodeProvider DEFAULT_GROUND = new NodeProvider() {
        @Override
        public @NotNull PathNode[] generateNodes(@NotNull PathfinderContext context, @NotNull PathNode nodeAt) {
            PathNode[] nodes = new PathNode[8];
            BlockCollisionProvider provider = context.blockProvider();
            MutableWorldVector at = nodeAt.asMutable();

            return nodes;
        }

        @Override
        public boolean mayTraverse(@NotNull PathfinderContext context, @NotNull PathAgent agent,
                                   @NotNull PathNode start, @NotNull PathNode next) {
            return false;
        }

        private void seekHighest(BlockCollisionProvider provider, MutableWorldVector from) {
            BlockCollisionSnapshot block = provider.getBlock(from);
            while(block != null && block.data().getMaterial().isSolid()) {
                block = provider.getBlock(from.add(Direction.UP));
            }
        }
    };

    /**
     * Generates an array of PathNode objects representing the points that the given PathAgent might be able to traverse
     * from a given 'origin' node. PathNode instances may be constructed from the origin node by using the origin's
     * add() or link() methods. Nodes generated this way will have their parent node set to the origin node.
     *
     * The returned array may contain nodes that can't be traversed. It is up to the implementation to perform a
     * detailed check, typically by calling mayTraverse, to ensure nodes are valid.
     * @param context The current PathfinderContext
     * @param nodeAt The PathNode we're pathfinding from
     * @return A null-terminated array of PathNode objects, which may be empty or contain null elements
     */
    public abstract @NotNull PathNode[] generateNodes(@NotNull PathfinderContext context, @NotNull PathNode nodeAt);

    /**
     * Returns true if PathAgent 'agent' may traverse the distance between the PathNode 'start' and PathNode 'next'.
     * PathOperations will typically use this method to perform collision checks, ensuring that the agent can actually
     * travel from "start" to "next".
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