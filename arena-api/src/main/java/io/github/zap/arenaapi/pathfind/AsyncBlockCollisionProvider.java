package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.nms.common.world.BlockSnapshot;
import io.github.zap.nms.common.world.BoxPredicate;
import io.github.zap.nms.common.world.CollisionChunkSnapshot;
import io.github.zap.vector.ChunkVectorAccess;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

class AsyncBlockCollisionProvider implements BlockCollisionProvider {
    private static final Map<ChunkIdentifier, CollisionChunkSnapshot> chunks = new ConcurrentHashMap<>();

    private final World world;
    private final int maxCaptureAge;

    AsyncBlockCollisionProvider(@NotNull World world, int maxCaptureAge) {
        this.world = world;
        this.maxCaptureAge = maxCaptureAge;
    }

    @Override
    public @NotNull World world() {
        return world;
    }

    @Override
    public boolean supportsAsync() {
        return true;
    }

    @Override
    public void updateRegion(@NotNull ChunkCoordinateProvider coordinates) {
        for(ChunkVectorAccess coordinate : coordinates) {
            ChunkIdentifier targetChunk = new ChunkIdentifier(world.getUID(), coordinate);

            if(world.isChunkLoaded(coordinate.chunkX(), coordinate.chunkZ())) {
                CollisionChunkSnapshot oldSnapshot = chunks.get(targetChunk);

                if(oldSnapshot == null || (Bukkit.getCurrentTick() - oldSnapshot.captureTick()) > maxCaptureAge) {
                    chunks.put(targetChunk, ArenaApi.getInstance().getNmsBridge().worldBridge()
                            .takeSnapshot(world.getChunkAt(coordinate.chunkX(), coordinate.chunkZ())));
                }
            }
            else {
                chunks.remove(targetChunk);
            }
        }
    }

    @Override
    public void clearRegion(@NotNull ChunkCoordinateProvider coordinates) {
        for(ChunkVectorAccess coordinate : coordinates) {
            chunks.remove(new ChunkIdentifier(world.getUID(), coordinate));
        }
    }

    @Override
    public void clearForWorld() {
        chunks.keySet().removeIf(id -> id.worldID.equals(world.getUID()));
    }

    @Override
    public boolean hasChunk(int x, int z) {
        return chunks.containsKey(new ChunkIdentifier(world.getUID(), ChunkVectorAccess.immutable(x, z)));
    }

    @Override
    public CollisionChunkSnapshot chunkAt(int x, int z) {
        return chunks.get(new ChunkIdentifier(world.getUID(), ChunkVectorAccess.immutable(x, z)));
    }

    @Override
    public @Nullable BlockSnapshot getBlock(int worldX, int worldY, int worldZ) {
        if(worldY < 0) {
            return null;
        }

        CollisionChunkSnapshot snapshot = chunkAt(worldX >> 4, worldZ >> 4);

        if(snapshot != null) {
            return snapshot.collisionSnapshot(worldX & 15, worldY, worldZ & 15);
        }

        return null;
    }

    @Override
    public boolean collidesWithAny(@NotNull BoundingBox worldRelativeBounds) {
        ChunkBoundsIterator iterator = new ChunkBoundsIterator(worldRelativeBounds);

        while(iterator.hasNext()) {
            CollisionChunkSnapshot chunk = iterator.next();

            if(chunk != null && chunk.collidesWithAny(worldRelativeBounds)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public @NotNull List<BlockSnapshot> collidingSolids(@NotNull BoundingBox worldRelativeBounds) {
        List<BlockSnapshot> shapes = new ArrayList<>();
        ChunkBoundsIterator iterator = new ChunkBoundsIterator(worldRelativeBounds);

        while(iterator.hasNext()) {
            CollisionChunkSnapshot chunk = iterator.next();

            if(chunk != null) {
                shapes.addAll(chunk.collisionsWith(worldRelativeBounds));
            }
        }

        return shapes;
    }

    private class ChunkBoundsIterator implements Iterator<CollisionChunkSnapshot> {
        private final int minChunkX;
        private final int maxChunkX;
        private final int maxChunkZ;

        int x;
        int z;

        private ChunkBoundsIterator(@NotNull BoundingBox worldRelativeBounds) {
            Vector min = worldRelativeBounds.getMin();
            Vector max = worldRelativeBounds.getMax();

            minChunkX = min.getBlockX() >> 4;
            maxChunkX = (max.getBlockX() >> 4) + 1;
            maxChunkZ = (max.getBlockZ() >> 4) + 1;

            x = minChunkX - 1;
            z = min.getBlockZ() >> 4;
        }

        @Override
        public boolean hasNext() {
            int nextX = x + 1;
            int nextZ = z;

            if(nextX == maxChunkX) {
                nextZ++;
            }

            return nextZ < maxChunkZ;
        }

        @Override
        public CollisionChunkSnapshot next() {
            if(++x == maxChunkX) {
                x = minChunkX;
                z++;
            }

            if(z >= maxChunkZ || x >= maxChunkX) {
                throw new IllegalStateException();
            }

            return chunkAt(x, z);
        }
    }
}
