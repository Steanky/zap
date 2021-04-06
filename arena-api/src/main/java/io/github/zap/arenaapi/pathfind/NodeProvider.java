package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.nms.common.world.WrappedVoxelShape;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementations of this interface provide PathNode objects. They are used by PathOperations to find out which nodes
 * may be traversed for a given agent, starting at the current node, in the current context. These nodes are directly
 * used by A* to determine the best path.
 *
 * In general, returning fewer nodes is better for memory usage and performance but may result in other problems such
 * as "coarse" paths that appear suboptimal to the user, even though A* is still finding the most optimal path given
 * its location. Returning more nodes may improve path appearance at the cost of performance.
 */
public interface NodeProvider {
    /**
     * NodeProvider that can be used to debug pathfinding — does not return nodes that change the Y-elevation or nodes
     * that enter a non-air block.
     */
    NodeProvider DEBUG = new NodeProvider() {
        @Override
        public @NotNull List<PathNode> generateNodes(@NotNull PathfinderContext context,
                                                     @NotNull PathAgent agent, @NotNull PathNode from) {
            List<PathNode> nextNodes = new ArrayList<>();

            PathNode up = from.add(1, 0, 0);
            PathNode right = from.add(0, 0, 1);
            PathNode down = from.add(-1, 0, 0);
            PathNode left = from.add(0, 0, -1);

            if(mayTraverse(context, agent, from, up)) {
                nextNodes.add(up);
            }

            if(mayTraverse(context, agent, from, right)) {
                nextNodes.add(right);
            }

            if(mayTraverse(context, agent, from, down)) {
                nextNodes.add(down);
            }

            if(mayTraverse(context, agent, from, left)) {
                nextNodes.add(left);
            }

            return nextNodes;
        }

        @Override
        public boolean mayTraverse(@NotNull PathfinderContext context, @NotNull PathAgent agent, @NotNull PathNode start, @NotNull PathNode next) {
            BlockData blockData = context.blockProvider().getData(next.x, next.y, next.z);
            WrappedVoxelShape shape = context.blockProvider().getCollision(next.x, next.y, next.z);

            if(shape != null) {
                ArenaApi.info("Found partial block at " + next);
            }

            return blockData != null && blockData.getMaterial().isAir();
        }
    };

    /**
     * NodeProvider that mimics vanilla pathfinding to an extent, with some improvements (precise collision shape is
     * taken into account)
     */
    NodeProvider SIMPLE_GROUND = new NodeProvider() {
        @Override
        public @NotNull List<PathNode> generateNodes(@NotNull PathfinderContext context, @NotNull PathAgent agent, @NotNull PathNode nodeAt) {
            PathAgent.Characteristics characteristics = agent.characteristics();
            return null;
        }

        @Override
        public boolean mayTraverse(@NotNull PathfinderContext context, @NotNull PathAgent agent, @NotNull PathNode start, @NotNull PathNode next) {
            return false;
        }
    };

    @NotNull List<PathNode> generateNodes(@NotNull PathfinderContext context, @NotNull PathAgent agent, @NotNull PathNode nodeAt);

    boolean mayTraverse(@NotNull PathfinderContext context, @NotNull PathAgent agent, @NotNull PathNode start, @NotNull PathNode next);
}