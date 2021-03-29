package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface NodeProvider {
    NodeProvider DEBUG = (context, operation, nodeAt) -> {
        SnapshotProvider snapshotProvider = context.snapshotProvider();
        List<PathNode> nextNodes = new ArrayList<>();

        BlockData blockNorth = snapshotProvider.getData(nodeAt.x + 1, nodeAt.y, nodeAt.z);
        BlockData blockEast = snapshotProvider.getData(nodeAt.x, nodeAt.y, nodeAt.z + 1);
        BlockData blockSouth = snapshotProvider.getData(nodeAt.x - 1, nodeAt.y, nodeAt.z);
        BlockData blockWest = snapshotProvider.getData(nodeAt.x, nodeAt.y, nodeAt.z - 1);

        if(blockNorth != null && blockNorth.getMaterial().isAir()) {
            nextNodes.add(nodeAt.add(1, 0, 0));
        }

        if(blockEast != null && blockEast.getMaterial().isAir()) {
            nextNodes.add(nodeAt.add(0, 0, 1));
        }

        if(blockSouth != null && blockSouth.getMaterial().isAir()) {
            nextNodes.add(nodeAt.add(-1, 0, 0));
        }

        if(blockWest != null && blockWest.getMaterial().isAir()) {
            nextNodes.add(nodeAt.add(0, 0, -1));
        }

        return nextNodes;
    };

    @NotNull List<PathNode> generateNodes(@NotNull PathfinderContext context, @NotNull PathOperation operation, @NotNull PathNode nodeAt);
}
