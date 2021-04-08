package io.github.zap.arenaapi.stats;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a cache of stats
 * @param <I> The identifier of the stats
 */
@RequiredArgsConstructor
public class StatsCache<I> {

    private final int maximumFreeCacheSize;

    private final Map<I, Stats<I>> cache = new HashMap<>();

    private final Map<I, Integer> cacheTaskCount = new ConcurrentHashMap<>();

    private final Map<I, Object> locks = new ConcurrentHashMap<>();

    private final Phaser pendingFlushPhaser = new Phaser(), pendingRequestPhaser = new Phaser();

    /**
     * Gets a value in the cache or computes it
     * @param identifier The identifier for the value
     * @param absenceMapping The mapping to determine the value if it is not present in the cache
     * @return The stats associated with the identifier
     */
    public @NotNull Stats<I> getValueFor(@NotNull I identifier, @NotNull Function<I, Stats<I>> absenceMapping) {
        pendingRequestPhaser.register();
        if (pendingFlushPhaser.getRegisteredParties() > 0) {
            pendingFlushPhaser.register();
            pendingRequestPhaser.arriveAndDeregister();
            pendingFlushPhaser.arriveAndAwaitAdvance();
            pendingRequestPhaser.register();
        }

        synchronized (getLockFor(identifier)) {
            Integer taskCount = cacheTaskCount.get(identifier);

            if (taskCount != null) {
                if (taskCount == 1) {
                    cacheTaskCount.remove(identifier);
                } else {
                    cacheTaskCount.put(identifier, taskCount - 1);
                }
            }

            pendingRequestPhaser.arriveAndDeregister();
            return cache.computeIfAbsent(identifier, absenceMapping);
        }
    }

    /**
     * Notifies the cache about a pending request for stats to better monitor if the cache should flush
     * @param identifier The identifier for the stats
     */
    public void notifyTaskFor(@NotNull I identifier) {
        synchronized (getLockFor(identifier)) {
            cacheTaskCount.merge(identifier, 1, Integer::sum);
        }
    }

    /**
     * Determines if the cache should be flushed
     * Not entirely threadsafe due to being an approximate measure
     * @return Whether the cache should be flushed
     */
    public boolean shouldFlush() {
        return cache.size() - cacheTaskCount.size() > maximumFreeCacheSize
                && pendingFlushPhaser.getRegisteredParties() <= 0;
    }

    private @NotNull Object getLockFor(@NotNull I identifier) {
        return locks.computeIfAbsent(identifier, (unused) -> new Object());
    }

    /**
     * Flushes the cache of any unnecessary values
     * @param callback A callback for each set of flushed stats
     */
    public void flush(@NotNull Consumer<Stats<I>> callback) {
        synchronized (this) {
            pendingFlushPhaser.register();
            pendingRequestPhaser.register();
            pendingRequestPhaser.arriveAndAwaitAdvance();

            Iterator<Map.Entry<I, Stats<I>>> iterator = cache.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<I, Stats<I>> next = iterator.next();
                callback.accept(next.getValue());

                // Remove item from the cache if there aren't any enqueued tasks that want to modify the stats
                if (!cacheTaskCount.containsKey(next.getKey())) {
                    iterator.remove();
                }
            }

            pendingFlushPhaser.arriveAndDeregister();
        }
    }

}
