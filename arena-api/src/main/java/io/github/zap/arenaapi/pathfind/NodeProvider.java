package io.github.zap.arenaapi.pathfind;

import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

public interface NodeProvider {
    NodeProvider DEBUG = (context, operation, nodeAt) -> {
        SnapshotProvider snapshotProvider = context.snapshotProvider();
        PathNode[] nextNodes = new PathNode[4];

        BlockData blockNorth = snapshotProvider.getData(nodeAt.x + 1, nodeAt.y, nodeAt.z);
        BlockData blockEast = snapshotProvider.getData(nodeAt.x, nodeAt.y, nodeAt.z + 1);
        BlockData blockSouth = snapshotProvider.getData(nodeAt.x - 1, nodeAt.y, nodeAt.z);
        BlockData blockWest = snapshotProvider.getData(nodeAt.x, nodeAt.y, nodeAt.z - 1);

        if(blockNorth != null && blockNorth.getMaterial().isAir()) {
            nextNodes[0] = nodeAt.add(1, 0, 0);
        }

        if(blockEast != null && blockEast.getMaterial().isAir()) {
            nextNodes[1] = nodeAt.add(0, 0, 1);
        }

        if(blockSouth != null && blockSouth.getMaterial().isAir()) {
            nextNodes[2] = nodeAt.add(-1, 0, 0);
        }

        if(blockWest != null && blockWest.getMaterial().isAir()) {
            nextNodes[3] = nodeAt.add(0, 0, -1);
        }

        return nextNodes;
    };

    PathNode[] generateNodes(@NotNull PathfinderContext context, @NotNull PathOperation operation, @NotNull PathNode nodeAt);
}
