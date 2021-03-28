package io.github.zap.arenaapi.pathfind;

import net.minecraft.server.v1_16_R3.IChunkAccess;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SnapshotProvider {
    @NotNull World getWorld();

    @Nullable ChunkSnapshot chunkAt(int x, int y);

    @Nullable ChunkSnapshot chunkAt(long key);

    boolean hasChunkAt(int x, int y);

    boolean hasChunkAt(long key);

    void updateChunk(int x, int y);

    void updateChunk(long key);

    void syncWithWorld();

    @Nullable
    BlockData getData(int x, int y, int z);
}