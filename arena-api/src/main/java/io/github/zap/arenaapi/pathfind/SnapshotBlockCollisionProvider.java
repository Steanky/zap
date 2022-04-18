package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.vector.Vector2I;
import io.github.zap.vector.Vectors;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

class SnapshotBlockCollisionProvider extends BlockCollisionProviderAbstract {
    private final Map<Vector2I, CollisionChunkView> chunkSnapshots = new ConcurrentHashMap<>();

    private final int maxCaptureAge;

    SnapshotBlockCollisionProvider(@NotNull World world, int maxCaptureAge) {
        super(world, true);
        this.maxCaptureAge = maxCaptureAge;
    }

    @Override
    public void updateRegion(@NotNull ChunkCoordinateProvider coordinates) {
        for(Vector2I coordinate : coordinates) {
            if(world.isChunkLoaded(coordinate.x(), coordinate.z())) {
                CollisionChunkView oldSnapshot = chunkSnapshots.get(coordinate);

                if(oldSnapshot == null || (Bukkit.getCurrentTick() - oldSnapshot.captureTick()) > maxCaptureAge) {
                    chunkSnapshots.put(coordinate, ArenaApi.getInstance().getNmsBridge().worldBridge()
                            .snapshotView(world.getChunkAt(coordinate.x(), coordinate.z())));
                }
            }
            else {
                chunkSnapshots.remove(coordinate);
            }
        }
    }

    @Override
    public void clearRegion(@NotNull ChunkCoordinateProvider coordinates) {
        for(Vector2I coordinate : coordinates) {
            chunkSnapshots.remove(coordinate);
        }
    }

    @Override
    public void clearForWorld() {
        chunkSnapshots.clear();
    }

    @Override
    public boolean hasChunk(int x, int z) {
        return chunkSnapshots.containsKey(Vectors.of(x, z));
    }

    @Override
    public CollisionChunkView chunkAt(int x, int z) {
        return chunkSnapshots.get(Vectors.of(x, z));
    }
}
