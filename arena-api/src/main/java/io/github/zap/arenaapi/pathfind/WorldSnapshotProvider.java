package io.github.zap.arenaapi.pathfind;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WorldSnapshotProvider implements SnapshotProvider {
    private final World world;
    private final Map<Long, ChunkSnapshot> chunks = new HashMap<>();

    public WorldSnapshotProvider(@NotNull World world) {
        this.world = Objects.requireNonNull(world, "world cannot be null!");
    }

    @Override
    public @NotNull World getWorld() {
        return world;
    }

    @Override
    public synchronized @Nullable ChunkSnapshot chunkAt(int x, int z) {
        return chunkAt(Chunk.getChunkKey(x, z));
    }

    @Override
    public synchronized @Nullable ChunkSnapshot chunkAt(long key) {
        return chunks.get(key);
    }

    @Override
    public synchronized boolean hasChunkAt(int x, int z) {
        return hasChunkAt(Chunk.getChunkKey(x, z));
    }

    @Override
    public synchronized boolean hasChunkAt(long key) {
        return chunks.get(key) != null;
    }

    @Override
    public synchronized void updateChunk(int x, int z) {
        updateChunk(Chunk.getChunkKey(x, z));
    }

    @Override
    public synchronized void updateChunk(long key) {
        if(chunks.containsKey(key)) {
            chunks.put(key, world.getChunkAt(key).getChunkSnapshot());
        }
    }

    @Override
    public synchronized void syncWithWorld() {
        chunks.clear();

        for(Chunk chunk : world.getLoadedChunks()) {
            chunks.put(chunk.getChunkKey(), chunk.getChunkSnapshot());
        }
    }

    @Override
    public synchronized @Nullable BlockData getData(int worldX, int worldY, int worldZ) {
        int chunkX = worldX / 16;
        int chunkZ = worldZ / 16;

        ChunkSnapshot snapshot = chunkAt(chunkX, chunkZ);
        if(snapshot != null) {
            return snapshot.getBlockData(worldX % 16, worldY, worldZ % 16);
        }

        return null;
    }
}
