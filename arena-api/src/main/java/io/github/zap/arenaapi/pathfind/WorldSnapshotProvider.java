package io.github.zap.arenaapi.pathfind;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class WorldSnapshotProvider implements SnapshotProvider {
    private static final Map<ChunkIdentifier, ChunkSnapshot> GLOBAL_CHUNKS = new HashMap<>();

    private final World world;
    private final ChunkRange range;

    WorldSnapshotProvider(@NotNull World world, @NotNull ChunkRange range) {
        this.world = world;
        this.range = range;
    }

    @Override
    public @NotNull World getWorld() {
        return world;
    }

    @Override
    public @Nullable ChunkSnapshot chunkAt(int x, int z) {
        return GLOBAL_CHUNKS.get(new ChunkIdentifier(world.getUID(), new ChunkCoordinate(x, z)));
    }

    @Override
    public boolean hasChunkAt(int x, int z) {
        return GLOBAL_CHUNKS.get(new ChunkIdentifier(world.getUID(), new ChunkCoordinate(x, z))) != null;
    }

    private void updateChunkInternal(int x, int z) {
        Chunk chunk = world.getChunkAt(x, z);
        GLOBAL_CHUNKS.put(new ChunkIdentifier(world.getUID(), new ChunkCoordinate(x, z)), chunk.getChunkSnapshot());
    }

    @Override
    public void updateChunk(int x, int z) {
        if(range.inRange(x, z) && world.isChunkLoaded(x, z)) {
            updateChunkInternal(x, z);
        }
    }

    @Override
    public void updateAll() {
        for(ChunkCoordinate coordinate : range) {
            if(world.isChunkLoaded(coordinate.x, coordinate.z)) {
                updateChunkInternal(coordinate.x, coordinate.z);
            }
            else {
                GLOBAL_CHUNKS.remove(new ChunkIdentifier(world.getUID(), coordinate));
            }
        }
    }

    @Override
    public @NotNull ChunkRange range() {
        return range;
    }

    @Override
    public @Nullable BlockData getData(int worldX, int worldY, int worldZ) {
        int chunkX = worldX / 16;
        int chunkZ = worldZ / 16;

        ChunkSnapshot snapshot = chunkAt(chunkX, chunkZ);
        if(snapshot != null) {
            int xM = worldX % 16;
            int zM = worldZ % 16;

            return snapshot.getBlockData(xM < 0 ? 16 + xM : xM, worldY, zM < 0 ? 16 + zM : zM);
        }

        return null;
    }
}
