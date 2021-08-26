package io.github.zap.arenaapi.pathfind;

import com.google.common.collect.MapMaker;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import io.github.zap.vector.Vector2I;
import io.github.zap.vector.Vectors;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;

class ProxyBlockCollisionProvider extends BlockCollisionProviderAbstract {
    private final Map<Vector2I, CollisionChunkView> chunkCache;

    private final WorldBridge worldBridge = ArenaApi.getInstance().getNmsBridge().worldBridge();

    ProxyBlockCollisionProvider(@NotNull World world, int concurrency) {
        super(world, true);
        chunkCache = new MapMaker().weakValues().concurrencyLevel(concurrency).makeMap();
    }

    @Override
    public boolean hasChunk(int x, int z) {
        //this is definitely safe to access async!
        return world.isChunkLoaded(x, z);
    }

    @Override
    public @Nullable CollisionChunkView chunkAt(int x, int z) {
        Vector2I loc = Vectors.of(x, z);
        CollisionChunkView view = chunkCache.get(loc);

        if(view == null) {
            Chunk chunk = worldBridge.getChunkIfLoadedImmediately(world, x, z);

            if(chunk != null) {
                view = worldBridge.proxyView(chunk);
                chunkCache.put(loc, view);
            }
        }

        return view;
    }
}
