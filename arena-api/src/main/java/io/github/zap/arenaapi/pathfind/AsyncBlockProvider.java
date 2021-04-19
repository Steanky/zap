package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.vector.ChunkVector;
import io.github.zap.nms.common.world.BlockCollisionSnapshot;
import io.github.zap.nms.common.world.CollisionChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class AsyncBlockProvider implements BlockProvider {
    private static final Map<ChunkIdentifier, CollisionChunkSnapshot> GLOBAL_CHUNKS = new HashMap<>();

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
        return GLOBAL_CHUNKS.get(new ChunkIdentifier(world.getUID(), new ChunkVector(x, z))) != null;
    }

    private void updateChunkInternal(int x, int z) {
        GLOBAL_CHUNKS.put(new ChunkIdentifier(world.getUID(), new ChunkVector(x, z)),
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
        for(ChunkVector coordinate : coordinateProvider) {
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

    private CollisionChunkSnapshot chunkAt(int x, int z) {
        return GLOBAL_CHUNKS.get(new ChunkIdentifier(world.getUID(), new ChunkVector(x, z)));
    }

    @Override
    public @Nullable BlockData getData(int worldX, int worldY, int worldZ) {
        int chunkX = worldX >> 4; //convert world coords to chunk coords
        int chunkZ = worldZ >> 4;

        CollisionChunkSnapshot snapshot = chunkAt(chunkX, chunkZ); //get the chunk we need
        if(snapshot != null) {
            int remX = worldX % 16;
            int remZ = worldZ % 16;

            return snapshot.getBlockData(remX < 0 ? 16 + remX : remX, worldY, remZ < 0 ? 16 + remZ : remZ);
        }

        return null;
    }

    @Override
    public @Nullable BlockCollisionSnapshot getCollision(int worldX, int worldY, int worldZ) {
        int chunkX = worldX >> 4;
        int chunkZ = worldZ >> 4;

        CollisionChunkSnapshot snapshot = chunkAt(chunkX, chunkZ);
        if(snapshot != null) {
            int remX = worldX % 16;
            int remZ = worldZ % 16;

            return snapshot.collisionSnapshot(remX < 0 ? 16 + remX : remX, worldY, remZ < 0 ? 16 + remZ : remZ);
        }

        return null;
    }
}
