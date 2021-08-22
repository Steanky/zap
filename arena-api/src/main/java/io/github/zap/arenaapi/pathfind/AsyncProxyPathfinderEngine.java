package io.github.zap.arenaapi.pathfind;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

public class AsyncProxyPathfinderEngine extends AsyncPathfinderEngineAbstract<PathfinderContext> {
    private static final int PATH_CAPACITY = 32;

    public AsyncProxyPathfinderEngine(@NotNull Plugin plugin) {
        super(new ConcurrentHashMap<>(), plugin);
    }

    @NotNull
    @Override
    protected PathfinderContext makeContext(@NotNull BlockCollisionProvider provider) {
        return new ProxyPathfinderContextImpl(provider, new PathMergerImpl(), PATH_CAPACITY);
    }

    @Override
    protected @NotNull BlockCollisionProvider makeBlockCollisionProvider(@NotNull World world) {
        return new ProxyBlockCollisionProvider(world, MAX_THREADS);
    }
}
