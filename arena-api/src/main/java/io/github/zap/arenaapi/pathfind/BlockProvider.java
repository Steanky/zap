package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.BlockVector;
import io.github.zap.arenaapi.vector.ChunkVector;
import io.github.zap.nms.common.world.BlockCollisionSnapshot;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockProvider {
    @NotNull World getWorld();

    boolean hasChunkAt(int x, int y);

    void updateChunk(int x, int y);

    default void updateChunk(@NotNull ChunkVector source) {
        updateChunk(source.chunkX(), source.chunkZ());
    }

    void updateAll();

    @NotNull ChunkCoordinateProvider coordinateProvider();

    //TODO: make getData method return a wrapper object that also contains the block collision
    @Nullable BlockData getData(int x, int y, int z);

    default BlockData getData(@NotNull BlockVector source) {
        return getData(source.blockX(), source.blockY(), source.blockZ());
    }

    @Nullable BlockCollisionSnapshot getCollision(int x, int y, int z);

    default @Nullable BlockCollisionSnapshot getCollision(@NotNull BlockVector source) {
        return getCollision(source.blockX(), source.blockY(), source.blockZ());
    }
}