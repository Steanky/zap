package io.github.zap.arenaapi.stats;

import io.github.zap.arenaapi.ArenaApi;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Manager for player statistics regarding an arena type
 */
public abstract class StatsManager {

    private final static ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10);

    private final static long EXPERIMENTALLY_DETERMINED_BEST_EXECUTOR_SERVICE_SHUTDOWN_TIME = 69L;

    private final Map<String, StatsCache<?, ?>> caches = new HashMap<>();

    /**
     * Queues statistic modification in a cache
     * @param cacheName The name of the cache
     * @param identifier The identifier for the stats in the cache
     * @param callback The callback to modify the stats
     * @param defaultMapping A default mapping for the stats value to be put into the cache if it does not exist
     * @param <I> The type of the identifier for the stats
     * @param <S> The type of the stats
     */
    public <I, S extends Stats<I>> void queueCacheModification(@NotNull String cacheName, @NotNull I identifier,
                                                               @NotNull Consumer<S> callback,
                                                               @NotNull Function<I, S> defaultMapping) {
        try {
            @SuppressWarnings("unchecked") StatsCache<I, S> cache = (StatsCache<I, S>) caches.get(cacheName);

            if (cache != null) {
                cache.notifyTaskFor(identifier);
                EXECUTOR_SERVICE.submit(() -> {
                   modifyCacheValue(cache, identifier, callback, defaultMapping);
                });
            } else {
                ArenaApi.warning("Attempted to modify a cache value for a cache that did not exist with cache name "
                        + cacheName + ", skipping cache modification");
            }
        } catch (ClassCastException e) {
            ArenaApi.warning("Attempted to modify a cache value with incorrect generic parameters with cache name "
                    + cacheName + ", skipping cache modification");
        }
    }

    /**
     * Modifies stats in a cache
     * @param cache The cache to modify values in
     * @param identifier The identifier for the stats in the cache
     * @param callback The callback to modify the stats
     * @param defaultMapping A default mapping for the stats value to be put into the cache if it does not exist
     * @param <I> The type of the identifier for the stats
     * @param <S> The type of the stats
     */
    private <I, S extends Stats<I>> void modifyCacheValue(@NotNull StatsCache<I, S> cache, @NotNull I identifier,
                                                          @NotNull Consumer<S> callback,
                                                          @NotNull Function<I, S> defaultMapping) {
        S stats = cache.getValueFor(identifier,
                (unused) -> loadStats(cache.getName(), identifier, cache.getStatsClass(), defaultMapping));
        callback.accept(stats);

        if (cache.shouldFlush()) {
            cache.flush((flushedStats) -> writeStats(cache.getName(), flushedStats));
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
     */
    public void destroy() {
        for (StatsCache<?, ?> cache : caches.values()) {
            cache.flush((flushedStats) -> writeStats(cache.getName(), flushedStats));
        }

        EXECUTOR_SERVICE.shutdown();
        try {
            //noinspection ResultOfMethodCallIgnored
            EXECUTOR_SERVICE.awaitTermination(EXPERIMENTALLY_DETERMINED_BEST_EXECUTOR_SERVICE_SHUTDOWN_TIME,
                    TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
