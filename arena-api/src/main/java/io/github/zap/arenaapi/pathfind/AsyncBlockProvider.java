package io.github.zap.arenaapi.pathfind;

import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class AsyncBlockProvider implements BlockProvider {
    private static final Map<ChunkIdentifier, ChunkSnapshot> GLOBAL_CHUNKS = new HashMap<>();

    private final World world;
    private final ChunkRange range;

    AsyncBlockProvider(@NotNull World world, @NotNull ChunkRange range) {
        this.world = world;
        this.range = range;
    }

    @Override
    public @NotNull World getWorld() {
        return world;
    }

    @Override
    public boolean hasChunkAt(int x, int z) {
        return GLOBAL_CHUNKS.get(new ChunkIdentifier(world.getUID(), new ChunkCoordinate(x, z))) != null;
    }

    private void updateChunkInternal(int x, int z) {
        GLOBAL_CHUNKS.put(new ChunkIdentifier(world.getUID(), new ChunkCoordinate(x, z)), world.getChunkAt(x, z).getChunkSnapshot());
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

    private ChunkSnapshot chunkAt(int x, int z) {
        return GLOBAL_CHUNKS.get(new ChunkIdentifier(world.getUID(), new ChunkCoordinate(x, z)));
    }

    @Override
    public @Nullable BlockData getData(int worldX, int worldY, int worldZ) {
        int chunkX = worldX >> 4; //convert world coords to chunk coords
        int chunkZ = worldZ >> 4;

        ChunkSnapshot snapshot = chunkAt(chunkX, chunkZ); //get the chunk we need
        if(snapshot != null) {
            int remX = worldX % 16;
            int remZ = worldZ % 16;

            return snapshot.getBlockData(remX < 0 ? 16 + remX : remX, worldY, remZ < 0 ? 16 + remZ : remZ);
        }

        return null;
    }
}
