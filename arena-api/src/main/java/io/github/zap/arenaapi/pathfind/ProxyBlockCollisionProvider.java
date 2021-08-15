package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import io.github.zap.vector.Vectors;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

class ProxyBlockCollisionProvider extends BlockCollisionProviderAbstract {
    private final Map<ChunkIdentifier, CollisionChunkView> chunkMap = new WeakHashMap<>();

    private final WorldBridge worldBridge = ArenaApi.getInstance().getNmsBridge().worldBridge();
    private final World world;

    ProxyBlockCollisionProvider(@NotNull World world) {
        super(world, true);
        this.world = world;
    }

    @Override
    public boolean hasChunk(int x, int z) {
        //this is definitely safe to access async!
        return world.isChunkLoaded(x, z);
    }

    @Override
    public @Nullable CollisionChunkView chunkAt(int x, int z) {
        ChunkIdentifier identifier = new ChunkIdentifier(world.getUID(), Vectors.of(x, z));
        CollisionChunkView view = chunkMap.get(identifier);

        if(view == null) {
            Chunk chunk = worldBridge.getChunkIfLoadedImmediately(world, x, z);
            if(chunk != null) {
                view = worldBridge.proxyView(chunk);
                chunkMap.put(identifier, view);
            }
        }

        return view;
    }
}
