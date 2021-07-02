package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.nms.common.world.BlockSnapshot;
import io.github.zap.nms.common.world.CollisionChunkSnapshot;
import io.github.zap.vector.ChunkVectorAccess;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class AsyncBlockCollisionProvider implements BlockCollisionProvider {
    private final Map<ChunkIdentifier, CollisionChunkSnapshot> chunks = new HashMap<>();

    private final World world;
    private final int minUpdateAge;

    AsyncBlockCollisionProvider(@NotNull World world, int minUpdateAge) {
        this.world = world;
        this.minUpdateAge = minUpdateAge;
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
    public void updateRegion(@NotNull ChunkCoordinateProvider coordinates) {
        for(ChunkVectorAccess coordinate : coordinates) {
            ChunkIdentifier targetChunk = new ChunkIdentifier(world.getUID(), coordinate);

            //TODO: make this more performant
            if(world.isChunkLoaded(coordinate.chunkX(), coordinate.chunkZ())) {
                chunks.put(targetChunk, ArenaApi.getInstance().getNmsBridge().worldBridge()
                        .takeSnapshot(world.getChunkAt(coordinate.chunkX(), coordinate.chunkZ())));
            }
            else {
                chunks.remove(new ChunkIdentifier(world.getUID(), coordinate));
            }
        }
    }

    private CollisionChunkSnapshot chunkAt(int x, int z) {
        return chunks.get(new ChunkIdentifier(world.getUID(), ChunkVectorAccess.immutable(x, z)));
    }

    @Override
    public @Nullable BlockSnapshot getBlock(int worldX, int worldY, int worldZ) {
        CollisionChunkSnapshot snapshot = chunkAt(worldX >> 4, worldZ >> 4);

        if(snapshot != null) {
            return snapshot.collisionSnapshot(worldX & 15, worldY, worldZ & 15);
        }

        return null;
    }

    @Override
    public boolean collidesWithAnySolid(@NotNull BoundingBox worldRelativeBounds) {
        Vector min = worldRelativeBounds.getMin();
        Vector max = worldRelativeBounds.getMax();

        int minChunkX = min.getBlockX() >> 4;
        int minChunkZ = min.getBlockZ() >> 4;

        int maxChunkX = max.getBlockX() >> 4;
        int maxChunkZ = max.getBlockZ() >> 4;

        for(int x = minChunkX; x <= maxChunkX; x++) {
            for(int z = minChunkZ; z <= maxChunkZ; z++) {
                CollisionChunkSnapshot chunk = chunkAt(x, z);

                if(chunk != null && chunk.collidesWithAny(worldRelativeBounds)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public @NotNull List<BlockSnapshot> collidingSolids(@NotNull BoundingBox worldRelativeBounds) {
        Vector min = worldRelativeBounds.getMin();
        Vector max = worldRelativeBounds.getMax();

        int minChunkX = min.getBlockX() >> 4;
        int minChunkZ = min.getBlockZ() >> 4;

        int maxChunkX = max.getBlockX() >> 4;
        int maxChunkZ = max.getBlockZ() >> 4;

        List<BlockSnapshot> shapes = new ArrayList<>();

        for(int x = minChunkX; x <= maxChunkX; x++) {
            for(int z = minChunkZ; z <= maxChunkZ; z++) {
                CollisionChunkSnapshot chunk = chunkAt(x, z);

                if(chunk != null) {
                    shapes.addAll(chunk.collisionsWith(worldRelativeBounds));
                }
            }
        }

        return shapes;
    }
}
