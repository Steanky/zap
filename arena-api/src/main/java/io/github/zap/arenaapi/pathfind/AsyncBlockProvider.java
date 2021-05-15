package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.vector.ChunkVectorAccess;
import io.github.zap.nms.common.world.BlockSnapshot;
import io.github.zap.nms.common.world.CollisionChunkSnapshot;
import org.bukkit.World;
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
        return GLOBAL_CHUNKS.containsKey(new ChunkIdentifier(world.getUID(), ChunkVectorAccess.immutable(x, z)));
    }

    private void updateChunkInternal(int x, int z) {
        GLOBAL_CHUNKS.put(new ChunkIdentifier(world.getUID(), ChunkVectorAccess.immutable(x, z)),
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
        for(ChunkVectorAccess coordinate : coordinateProvider) {
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
        return GLOBAL_CHUNKS.get(new ChunkIdentifier(world.getUID(), ChunkVectorAccess.immutable(x, z)));
    }

    @Override
    public @Nullable BlockSnapshot getBlock(int worldX, int worldY, int worldZ) {
        int chunkX = worldX >> 4;
        int chunkZ = worldZ >> 4;

        CollisionChunkSnapshot snapshot = chunkAt(chunkX, chunkZ);
        if(snapshot != null) {
            return snapshot.blockSnapshot(worldX & 15, worldY, worldZ & 15);
        }

        return null;
    }

    @Override
    public boolean collisionAt(double x, double y, double z) {
        int blockX = (int)x;
        int blockY = (int)y;
        int blockZ = (int)z;

        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;

        CollisionChunkSnapshot snapshot = chunkAt(chunkX, chunkZ);
        if(snapshot != null) {
            snapshot.blockSnapshot(chunkX, blockY, chunkZ);
        }

        return false;
    }
}
