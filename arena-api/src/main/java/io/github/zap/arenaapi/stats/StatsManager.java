package io.github.zap.arenaapi.stats;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class StatsManager {

    private final static ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10);

    private final Map<String, StatsCache<?>> caches = new HashMap<>();



    /**
     * Registers a cache for the stats manager
     * @param name The name of the cache
     * @param cache The cache to register
     */
    public void registerCache(@NotNull String name, @NotNull StatsCache<?> cache) {
        caches.put(name, cache);
    }

}
