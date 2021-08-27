package io.github.zap.arenaapi.pathfind.engine;

import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProviders;
import io.github.zap.arenaapi.pathfind.context.PathfinderContext;
import io.github.zap.arenaapi.pathfind.context.PathfinderContexts;
import io.github.zap.arenaapi.pathfind.process.PathMergers;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

class AsyncProxyPathfinderEngine extends AsyncPathfinderEngineAbstract<PathfinderContext> {
    private static final int PATH_CAPACITY = 32;

    AsyncProxyPathfinderEngine(@NotNull Plugin plugin) {
        super(new ConcurrentHashMap<>(), plugin);
    }

    @NotNull
    @Override
    protected PathfinderContext makeContext(@NotNull BlockCollisionProvider provider) {
        plugin.getLogger().log(Level.INFO, "Pathfinder context created for world " + provider.world().getName());
        return PathfinderContexts.proxyContext(provider, PathMergers.defaultMerger(), PATH_CAPACITY);
    }

    @Override
    protected @NotNull BlockCollisionProvider makeBlockCollisionProvider(@NotNull World world) {
        return BlockCollisionProviders.proxyAsyncProvider(world, MAX_THREADS);
    }
}
