package io.github.zap.arenaapi.pathfind;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

class AsyncProxyPathfinderEngine extends AsyncPathfinderEngineAbstract<PathfinderContext> {
    private static final AsyncProxyPathfinderEngine INSTANCE = new AsyncProxyPathfinderEngine();
    private static final int PATH_CAPACITY = 32;

    private AsyncProxyPathfinderEngine() {
        super(new ConcurrentHashMap<>());
    }

    public static AsyncProxyPathfinderEngine getInstance() {
        return INSTANCE;
    }

    @NotNull
    @Override
    protected PathfinderContext makeContext(@NotNull BlockCollisionProvider provider) {
        return new ProxyPathfinderContextImpl(provider, new PathMergerImpl(), PATH_CAPACITY);
    }

    @Override
    protected @NotNull BlockCollisionProvider getBlockCollisionProvider(@NotNull World world) {
        return new ProxyBlockCollisionProvider(world);
    }
}
