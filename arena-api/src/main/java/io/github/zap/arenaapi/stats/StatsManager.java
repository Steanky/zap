package io.github.zap.arenaapi.stats;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * Manager for player statistics regarding an arena type
 */
public abstract class StatsManager {

    private final static long EXPERIMENTALLY_DETERMINED_BEST_EXECUTOR_SERVICE_SHUTDOWN_TIME = 69L;

    private final Map<String, StatsCache<?, ?>> caches = new HashMap<>();

    private final Plugin plugin;

    private final ExecutorService executorService;

    private final long shutdownTime;

    public StatsManager(@NotNull Plugin plugin, @NotNull ExecutorService executorService, long shutdownTime) {
        this.plugin = plugin;
        this.executorService = executorService;
        this.shutdownTime = shutdownTime;
    }

    public StatsManager(@NotNull Plugin plugin, @NotNull ExecutorService executorService) {
        this(plugin, executorService, EXPERIMENTALLY_DETERMINED_BEST_EXECUTOR_SERVICE_SHUTDOWN_TIME);
    }

    /**
     * Queues statistic request in a cache
     * @param cacheName The name of the cache
     * @param identifier The identifier for the stats in the cache
     * @param defaultMapping A default mapping for the stats value to be put into the cache if it does not exist
     * @param callback The callback to modify the stats
     * @param <I> The type of the identifier for the stats
     * @param <S> The type of the stats
     */
    public <I, S extends Stats<I>> void queueCacheRequest(@NotNull String cacheName, @NotNull I identifier,
                                                          @NotNull Function<I, S> defaultMapping,
                                                          @NotNull Consumer<S> callback) {
        try {
            @SuppressWarnings("unchecked") StatsCache<I, S> cache = (StatsCache<I, S>) caches.get(cacheName);

            if (cache != null) {
                cache.notifyTaskFor(identifier);
                executorService.submit(() -> getCacheValue(cache, identifier, defaultMapping, callback));
            } else {
                plugin.getLogger().warning("Attempted to modify a cache value for a cache that did not exist " +
                        "with cache name " + cacheName + ", skipping cache modification");
            }
        } catch (ClassCastException e) {
            plugin.getLogger().warning("Attempted to modify a cache value with incorrect generic parameters " +
                    "with cache name " + cacheName + ", skipping cache modification");
        }
    }

    /**
     * Gets stats in a cache
     * @param cache The cache to modify values in
     * @param identifier The identifier for the stats in the cache
     * @param defaultMapping A default mapping for the stats value to be put into the cache if it does not exist
     * @param callback The callback to modify the stats
     * @param <I> The type of the identifier for the stats
     * @param <S> The type of the stats
     */
    private <I, S extends Stats<I>> void getCacheValue(@NotNull StatsCache<I, S> cache, @NotNull I identifier,
                                                       @NotNull Function<I, S> defaultMapping,
                                                       @NotNull Consumer<S> callback) {
        cache.getValueFor(identifier,
                (unused) -> loadStats(cache.getName(), identifier, cache.getStatsClass(), defaultMapping), callback);

        if (cache.shouldFlush()) {
            cache.flush(flushedStats -> writeStats(cache.getName(), flushedStats));
        }
    }

    /**
     * Registers a cache for the stats manager
     * @param cache The cache to register
     */
    public void registerCache(@NotNull StatsCache<?, ?> cache) {
        caches.put(cache.getName(), cache);
    }

    /**
     * Destroys the stats manager and writes all caches to storage
     * @return Whether destruction was successful
     */
    public boolean destroy() {
        executorService.shutdown(); // todo static in instance method, change that
        try {
            if (executorService.awaitTermination(shutdownTime, TimeUnit.SECONDS)) {
                // once all requests have gone through, flush all caches
                for (StatsCache<?, ?> cache : caches.values()) {
                    cache.flush(flushedStats -> writeStats(cache.getName(), flushedStats));
                }

                return true;
            }
        } catch (InterruptedException exception) {
            plugin.getLogger().log(Level.SEVERE, "The stats manager was interrupted while shutting down.",
                    exception);
        }

        plugin.getLogger().log(Level.SEVERE, "The stats manager was unable to shutdown in " +
                EXPERIMENTALLY_DETERMINED_BEST_EXECUTOR_SERVICE_SHUTDOWN_TIME + " seconds. Some data will be lost.");
        return false;
    }

    /**
     * Gets the plugin associated with this stats
     * @return The plugin
     */
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    /**
     * Loads stats from storage
     * @param identifier The identifier of the stats
     * @param <I> The type of the identifier for the stats
     * @param <S> The type of the stats
     * @return The loaded stats or a generated replacement value
     */
    protected abstract <I, S extends Stats<I>> @NotNull S loadStats(@NotNull String cacheName, @NotNull I identifier,
                                                                    @NotNull Class<S> clazz,
                                                                    @NotNull Function<I, S> callback);

    /**
     * Writes stats to storage
     * @param stats The stats to write to storage
     * @param <I> The type of the identifier for the stats
     * @param <S> The type of the stats
     */
    protected abstract <I, S extends Stats<I>> void writeStats(@NotNull String cacheName, @NotNull S stats);

}
