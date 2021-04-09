package io.github.zap.arenaapi.pathfind;

import io.github.zap.nms.common.world.VoxelShapeWrapper;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockProvider {
    @NotNull World getWorld();

    boolean hasChunkAt(int x, int y);

    void updateChunk(int x, int y);

    void updateAll();

    @NotNull ChunkRange range();

    @Nullable BlockData getData(int x, int y, int z);

    @Nullable VoxelShapeWrapper getCollision(int x, int y, int z);
}