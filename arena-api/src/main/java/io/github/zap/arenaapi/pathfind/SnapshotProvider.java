package io.github.zap.arenaapi.pathfind;

import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SnapshotProvider {
    @NotNull World getWorld();

    @Nullable ChunkSnapshot chunkAt(int x, int y);

    boolean hasChunkAt(int x, int y);

    void updateChunk(int x, int y);

    void updateAll();

    @NotNull ChunkRange range();

    @Nullable BlockData getData(int x, int y, int z);
}