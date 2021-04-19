package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.ChunkVector;
import io.github.zap.arenaapi.vector.WorldVector;
import io.github.zap.nms.common.world.BlockSnapshot;
import org.bukkit.World;
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

    @Nullable BlockSnapshot getBlock(int x, int y, int z);

    default @Nullable BlockSnapshot getBlock(@NotNull WorldVector source) {
        return getBlock(source.blockX(), source.blockY(), source.blockZ());
    }
}