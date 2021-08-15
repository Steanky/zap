package io.github.zap.arenaapi.pathfind;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

class AsyncProxyPathfinderEngine extends AsyncPathfinderEngineAbstract<PathfinderContext> {
    private static final int PATH_CAPACITY = 32;

    AsyncProxyPathfinderEngine() {
        super(new ConcurrentHashMap<>());
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
