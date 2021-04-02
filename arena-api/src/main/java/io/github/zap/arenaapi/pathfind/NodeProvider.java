package io.github.zap.arenaapi.pathfind;

import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface NodeProvider {
    NodeProvider DEBUG = new NodeProvider() {
        @Override
        public @NotNull List<PathNode> generateValidNodes(@NotNull PathfinderContext context,
                                                          @NotNull PathAgent agent, @NotNull PathNode nodeAt) {
            List<PathNode> nextNodes = new ArrayList<>();

            PathNode up = nodeAt.add(1, 0, 0);
            PathNode right = nodeAt.add(0, 0, 1);
            PathNode down = nodeAt.add(-1, 0, 0);
            PathNode left = nodeAt.add(0, 0, -1);

            if(isValid(context, agent, nodeAt, up)) {
                nextNodes.add(up);
            }

            if(isValid(context, agent, nodeAt, right)) {
                nextNodes.add(right);
            }

            if(isValid(context, agent, nodeAt, down)) {
                nextNodes.add(down);
            }

            if(isValid(context, agent, nodeAt, left)) {
                nextNodes.add(left);
            }

            return nextNodes;
        }

        @Override
        public boolean isValid(@NotNull PathfinderContext context, @NotNull PathAgent agent, @NotNull PathNode start, @NotNull PathNode next) {
            BlockData blockData = context.blockProvider().getData(next.x, next.y, next.z);
            return blockData != null && blockData.getMaterial().isAir();
        }
    };

    @NotNull List<PathNode> generateValidNodes(@NotNull PathfinderContext context, @NotNull PathAgent agent, @NotNull PathNode nodeAt);

    boolean isValid(@NotNull PathfinderContext context, @NotNull PathAgent agent, @NotNull PathNode start, @NotNull PathNode next);
}
