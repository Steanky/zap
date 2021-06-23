package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.vector.ChunkVectorAccess;
import io.github.zap.nms.common.world.BlockCollisionSnapshot;
import io.github.zap.nms.common.world.CollisionChunkSnapshot;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class AsyncBlockCollisionProvider implements BlockCollisionProvider {
    private final Map<ChunkIdentifier, CollisionChunkSnapshot> chunks = new HashMap<>();

    private final World world;
    private final ChunkCoordinateProvider coordinateProvider;

    AsyncBlockCollisionProvider(@NotNull World world, @NotNull ChunkCoordinateProvider coordinateProvider) {
        this.world = world;
        this.coordinateProvider = coordinateProvider;
    }

    @Override
    public @NotNull World getWorld() {
        return world;
    }

    @Override
    public boolean supportsAsync() {
        return true;
    }

    @Override
    public boolean hasChunkAt(int x, int z) {
        return chunks.containsKey(new ChunkIdentifier(world.getUID(), ChunkVectorAccess.immutable(x, z)));
    }

    private void updateChunkInternal(int x, int z) {
        chunks.put(new ChunkIdentifier(world.getUID(), ChunkVectorAccess.immutable(x, z)),
                ArenaApi.getInstance().getNmsBridge().worldBridge().takeSnapshot(world.getChunkAt(x, z)));
    }

    @Override
    public void updateChunk(int x, int z) {
        if(coordinateProvider.hasChunk(x, z)) {
            if(world.isChunkLoaded(x, z)) {
                updateChunkInternal(x, z);
            }
            else {
                chunks.remove(new ChunkIdentifier(world.getUID(), ChunkVectorAccess.immutable(x, z)));
            }
        }
    }

    @Override
    public void updateAll() {
        for(ChunkVectorAccess coordinate : coordinateProvider) {
            if(world.isChunkLoaded(coordinate.chunkX(), coordinate.chunkZ())) {
                updateChunkInternal(coordinate.chunkX(), coordinate.chunkZ());
            }
            else {
                chunks.remove(new ChunkIdentifier(world.getUID(), coordinate));
            }
        }
    }

    @Override
    public @NotNull ChunkCoordinateProvider coordinateProvider() {
        return coordinateProvider;
    }

    private CollisionChunkSnapshot chunkAt(int x, int z) {
        return chunks.get(new ChunkIdentifier(world.getUID(), ChunkVectorAccess.immutable(x, z)));
    }

    @Override
    public @Nullable BlockCollisionSnapshot getBlock(int worldX, int worldY, int worldZ) {
        int chunkX = worldX >> 4;
        int chunkZ = worldZ >> 4;

        CollisionChunkSnapshot snapshot = chunkAt(chunkX, chunkZ);
        if(snapshot != null) {
            return snapshot.blockCollisionSnapshot(worldX & 15, worldY, worldZ & 15);
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
            snapshot.blockCollisionSnapshot(chunkX, blockY, chunkZ);
        }

        return false;
    }
}
