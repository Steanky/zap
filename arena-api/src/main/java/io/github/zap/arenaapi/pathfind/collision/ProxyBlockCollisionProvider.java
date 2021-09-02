package io.github.zap.arenaapi.pathfind.collision;

import com.google.common.collect.MapMaker;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import io.github.zap.vector.Vector2I;
import io.github.zap.vector.Vectors;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

class ProxyBlockCollisionProvider extends BlockCollisionProviderAbstract {
    private final WorldBridge worldBridge;
    private final int concurrency;
    private final long timeoutInterval;
    private final TimeUnit timeoutUnit;

    ProxyBlockCollisionProvider(@NotNull WorldBridge worldBridge, @NotNull World world, int concurrency,
                                long timeoutInterval, @NotNull TimeUnit timeoutUnit) {
        super(world, new MapMaker().weakValues().concurrencyLevel(concurrency).makeMap(), true);
        this.worldBridge = worldBridge;
        this.concurrency = concurrency;
        this.timeoutInterval = timeoutInterval;
        this.timeoutUnit = timeoutUnit;
    }

    @Override
    public boolean hasChunk(int x, int z) {
        //this is definitely safe to access async!
        return world.isChunkLoaded(x, z);
    }

    @Override
    public @Nullable CollisionChunkView chunkAt(int x, int z) {
        long key = chunkKey(x, z);
        CollisionChunkView view = chunkViewMap.get(key);

        if(view == null) {
            Chunk chunk = worldBridge.getChunkIfLoadedImmediately(world, x, z);

            if(chunk != null) {
                view = worldBridge.proxyView(chunk, concurrency, timeoutInterval, timeoutUnit);
                chunkViewMap.put(key, view);
            }
        }

        return view;
    }
}
