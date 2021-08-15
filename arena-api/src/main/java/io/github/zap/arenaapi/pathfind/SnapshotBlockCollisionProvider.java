package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.vector.Vector2I;
import io.github.zap.vector.Vectors;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class SnapshotBlockCollisionProvider extends BlockCollisionProviderAbstract {
    private static final Map<ChunkIdentifier, CollisionChunkView> globalChunks = new ConcurrentHashMap<>();

    private final int maxCaptureAge;

    SnapshotBlockCollisionProvider(@NotNull World world, int maxCaptureAge) {
        super(world, true);
        this.maxCaptureAge = maxCaptureAge;
    }

    @Override
    public void updateRegion(@NotNull ChunkCoordinateProvider coordinates) {
        for(Vector2I coordinate : coordinates) {
            ChunkIdentifier targetChunk = new ChunkIdentifier(world.getUID(), coordinate);

            if(world.isChunkLoaded(coordinate.x(), coordinate.z())) {
                CollisionChunkView oldSnapshot = globalChunks.get(targetChunk);

                if(oldSnapshot == null || (Bukkit.getCurrentTick() - oldSnapshot.captureTick()) > maxCaptureAge) {
                    globalChunks.put(targetChunk, ArenaApi.getInstance().getNmsBridge().worldBridge()
                            .snapshotView(world.getChunkAt(coordinate.x(), coordinate.z())));
                }
            }
            else {
                globalChunks.remove(targetChunk);
            }
        }
    }

    @Override
    public void clearRegion(@NotNull ChunkCoordinateProvider coordinates) {
        for(Vector2I coordinate : coordinates) {
            globalChunks.remove(new ChunkIdentifier(world.getUID(), coordinate));
        }
    }

    @Override
    public void clearForWorld() {
        globalChunks.keySet().removeIf(id -> id.worldID.equals(world.getUID()));
    }

    @Override
    public boolean hasChunk(int x, int z) {
        return globalChunks.containsKey(new ChunkIdentifier(world.getUID(), Vectors.of(x, z)));
    }

    @Override
    public CollisionChunkView chunkAt(int x, int z) {
        return globalChunks.get(new ChunkIdentifier(world.getUID(), Vectors.of(x, z)));
    }
}
