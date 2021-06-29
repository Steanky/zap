package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.nms.common.world.BlockSnapshot;
import io.github.zap.nms.common.world.CollisionChunkSnapshot;
import io.github.zap.vector.ChunkVectorAccess;
import io.github.zap.vector.VectorAccess;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull BlockSnapshot getBlock(int worldX, int worldY, int worldZ) {
        CollisionChunkSnapshot snapshot = chunkAt(worldX >> 4, worldZ >> 4);
        if(snapshot != null) {
            return snapshot.collisionSnapshot(worldX & 15, worldY, worldZ & 15);
        }

        throw new IllegalArgumentException("Block at [" + worldX + ", " + worldY + ", " + worldZ + "] out of range!");
    }

    @Override
    public boolean collidesWithAnySolid(@NotNull BoundingBox worldRelativeBounds) {
        ChunkVectorAccess minChunk = VectorAccess.immutable(worldRelativeBounds.getMin());
        ChunkVectorAccess maxChunk = VectorAccess.immutable(worldRelativeBounds.getMax());

        for(int x = minChunk.chunkX(); x <= maxChunk.chunkX(); x++) {
            for(int z = minChunk.chunkZ(); z <= maxChunk.chunkZ(); z++) {
                CollisionChunkSnapshot chunk = chunks.get(new ChunkIdentifier(world.getUID(), ChunkVectorAccess.immutable(x, z)));

                if(chunk != null) {
                    return chunk.collidesWithAny(worldRelativeBounds);
                }
            }
        }

        return false;
    }

    @Override
    public List<BlockSnapshot> collidingSolids(@NotNull BoundingBox worldRelativeBounds) {
        ChunkVectorAccess minChunk = VectorAccess.immutable(worldRelativeBounds.getMin());
        ChunkVectorAccess maxChunk = VectorAccess.immutable(worldRelativeBounds.getMax());

        List<BlockSnapshot> shapes = new ArrayList<>();

        for(int x = minChunk.chunkX(); x <= maxChunk.chunkX(); x++) {
            for(int z = minChunk.chunkZ(); z <= maxChunk.chunkZ(); z++) {
                CollisionChunkSnapshot chunk = chunks.get(new ChunkIdentifier(world.getUID(), ChunkVectorAccess.immutable(x, z)));

                if(chunk != null) {
                    shapes.addAll(chunk.collisionsWith(worldRelativeBounds));
                }
            }
        }

        return shapes;
    }
}
