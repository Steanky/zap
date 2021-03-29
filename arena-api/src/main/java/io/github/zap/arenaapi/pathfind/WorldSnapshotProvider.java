package io.github.zap.arenaapi.pathfind;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class WorldSnapshotProvider implements SnapshotProvider {
    private final World world;
    private final Map<Long, ChunkSnapshot> chunks = new HashMap<>();

    WorldSnapshotProvider(@NotNull World world) {
        this.world = world;
    }

    @Override
    public @NotNull World getWorld() {
        return world;
    }

    @Override
    public @Nullable ChunkSnapshot chunkAt(int x, int z) {
        return chunkAt(Chunk.getChunkKey(x, z));
    }

    @Override
    public @Nullable ChunkSnapshot chunkAt(long key) {
        return chunks.get(key);
    }

    @Override
    public boolean hasChunkAt(int x, int z) {
        return hasChunkAt(Chunk.getChunkKey(x, z));
    }

    @Override
    public boolean hasChunkAt(long key) {
        return chunks.containsKey(key);
    }

    @Override
    public void updateChunk(int x, int z) {
        updateChunk(Chunk.getChunkKey(x, z));
    }

    @Override
    public void updateChunk(long key) {
        if(chunks.containsKey(key)) {
            chunks.put(key, world.getChunkAt(key).getChunkSnapshot());
        }
    }

    @Override
    public void syncWithWorld() {
        chunks.clear();

        for(Chunk chunk : world.getLoadedChunks()) {
            chunks.put(chunk.getChunkKey(), chunk.getChunkSnapshot());
        }
    }

    @Override
    public @Nullable BlockData getData(int worldX, int worldY, int worldZ) {
        int chunkX = worldX / 16;
        int chunkZ = worldZ / 16;

        ChunkSnapshot snapshot = chunkAt(chunkX, chunkZ);
        if(snapshot != null) {
            return snapshot.getBlockData(worldX % 16, worldY, worldZ % 16);
        }

        return null;
    }
}
