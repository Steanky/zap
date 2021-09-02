package io.github.zap.arenaapi.pathfind.collision;

import com.google.common.collect.MapMaker;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import io.github.zap.arenaapi.pathfind.chunk.ChunkBounds;
import io.github.zap.vector.Vector2I;
import io.github.zap.vector.Vectors;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

class SnapshotBlockCollisionProvider extends BlockCollisionProviderAbstract {
    private final WorldBridge worldBridge;
    private final int maxCaptureAge;


    SnapshotBlockCollisionProvider(@NotNull WorldBridge worldBridge, @NotNull World world, int concurrency, int maxCaptureAge) {
        super(world, new MapMaker().weakValues().concurrencyLevel(concurrency).makeMap(), true);
        this.worldBridge = worldBridge;
        this.maxCaptureAge = maxCaptureAge;
    }

    @Override
    public void updateRegion(@NotNull ChunkBounds coordinates) {
        for(Vector2I coordinate : coordinates) {
            long key = chunkKey(coordinate.x(), coordinate.z());

            if(world.isChunkLoaded(coordinate.x(), coordinate.z())) {
                CollisionChunkView oldSnapshot = chunkViewMap.get(key);

                if(oldSnapshot == null || (Bukkit.getCurrentTick() - oldSnapshot.captureTick()) > maxCaptureAge) {
                    chunkViewMap.put(key, worldBridge.snapshotView(world.getChunkAt(coordinate.x(), coordinate.z())));
                }
            }
            else {
                chunkViewMap.remove(key);
            }
        }
    }

    @Override
    public void clearRegion(@NotNull ChunkBounds coordinates) {
        for(Vector2I coordinate : coordinates) {
            chunkViewMap.remove(chunkKey(coordinate.x(), coordinate.z()));
        }
    }

    @Override
    public void clearForWorld() {
        chunkViewMap.clear();
    }

    @Override
    public boolean hasChunk(int x, int z) {
        return chunkViewMap.containsKey(chunkKey(x, z));
    }

    @Override
    public CollisionChunkView chunkAt(int x, int z) {
        return chunkViewMap.get(chunkKey(x, z));
    }
}
