package io.github.zap.arenaapi.pathfind.engine;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class PathfinderEngines {
    /**
     * Returns the default asynchronous implementation of PathfinderEngine.
     * @return An asynchronous PathfinderEngine implementation
     */
    public static PathfinderEngine async(@NotNull Plugin plugin) {
        return new AsyncSnapshotPathfinderEngine(plugin);
    }

    public static PathfinderEngine proxyAsync(@NotNull Plugin plugin) {
        return new AsyncProxyPathfinderEngine(plugin);
    }
}