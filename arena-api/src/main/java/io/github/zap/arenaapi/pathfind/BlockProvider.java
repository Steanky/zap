package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.BlockVectorSource;
import io.github.zap.arenaapi.vector.ChunkVectorSource;
import io.github.zap.nms.common.world.VoxelShapeWrapper;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockProvider {
    @NotNull World getWorld();

    boolean hasChunkAt(int x, int y);

    void updateChunk(int x, int y);

    default void updateChunk(@NotNull ChunkVectorSource source) {
        updateChunk(source.chunkX(), source.chunkZ());
    }

    void updateAll();

    @NotNull ChunkCoordinateProvider coordinateProvider();

    @Nullable BlockData getData(int x, int y, int z);

    default BlockData getData(@NotNull BlockVectorSource source) {
        return getData(source.blockX(), source.blockY(), source.blockZ());
    }

    @Nullable VoxelShapeWrapper getCollision(int x, int y, int z);

    default VoxelShapeWrapper getCollision(@NotNull BlockVectorSource source) {
        return getCollision(source.blockX(), source.blockY(), source.blockZ());
    }
}