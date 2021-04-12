package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.nms.common.world.SimpleChunkSnapshot;
import io.github.zap.nms.common.world.VoxelShapeWrapper;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class AsyncBlockProvider implements BlockProvider {
    private static final Map<ChunkIdentifier, SimpleChunkSnapshot> GLOBAL_CHUNKS = new HashMap<>();

    private final World world;
    private final ChunkCoordinateProvider coordinateProvider;

    AsyncBlockProvider(@NotNull World world, @NotNull ChunkCoordinateProvider coordinateProvider) {
        this.world = world;
        this.coordinateProvider = coordinateProvider;
    }

    @Override
    public @NotNull World getWorld() {
        return world;
    }

    @Override
    public boolean hasChunkAt(int x, int z) {
        return GLOBAL_CHUNKS.get(new ChunkIdentifier(world.getUID(), ChunkVectorSource.fromChunkCoordinate(x, z))) != null;
    }

    private void updateChunkInternal(int x, int z) {
        GLOBAL_CHUNKS.put(new ChunkIdentifier(world.getUID(), ChunkVectorSource.fromChunkCoordinate(x, z)),
                ArenaApi.getInstance().getNmsBridge().worldBridge().takeSnapshot(world.getChunkAt(x, z)));
    }

    @Override
    public void updateChunk(int x, int z) {
        if(coordinateProvider.hasChunk(x, z) && world.isChunkLoaded(x, z)) {
            updateChunkInternal(x, z);
        }
    }

    @Override
    public void updateAll() {
        for(ChunkVectorSource coordinate : coordinateProvider) {
            if(world.isChunkLoaded(coordinate.chunkX(), coordinate.chunkZ())) {
                updateChunkInternal(coordinate.chunkX(), coordinate.chunkZ());
            }
            else {
                GLOBAL_CHUNKS.remove(new ChunkIdentifier(world.getUID(), coordinate));
            }
        }
    }

    @Override
    public @NotNull ChunkCoordinateProvider coordinateProvider() {
        return coordinateProvider;
    }

    private SimpleChunkSnapshot chunkAt(int x, int z) {
        return GLOBAL_CHUNKS.get(new ChunkIdentifier(world.getUID(), ChunkVectorSource.fromChunkCoordinate(x, z)));
    }

    @Override
    public @Nullable BlockData getData(int worldX, int worldY, int worldZ) {
        int chunkX = worldX >> 4; //convert world coords to chunk coords
        int chunkZ = worldZ >> 4;

        SimpleChunkSnapshot snapshot = chunkAt(chunkX, chunkZ); //get the chunk we need
        if(snapshot != null) {
            int remX = worldX % 16;
            int remZ = worldZ % 16;

            return snapshot.getBlockData(remX < 0 ? 16 + remX : remX, worldY, remZ < 0 ? 16 + remZ : remZ);
        }

        return null;
    }

    @Override
    public @Nullable VoxelShapeWrapper getCollision(int worldX, int worldY, int worldZ) {
        int chunkX = worldX >> 4;
        int chunkZ = worldZ >> 4;

        SimpleChunkSnapshot snapshot = chunkAt(chunkX, chunkZ);
        if(snapshot != null) {
            int remX = worldX % 16;
            int remZ = worldZ % 16;

            return snapshot.collisionFor(remX < 0 ? 16 + remX : remX, worldY, remZ < 0 ? 16 + remZ : remZ);
        }

        return null;
    }
}
