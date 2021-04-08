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
import java.util.function.Supplier;

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
        return accessCacheMember(identifier, () -> {
            Integer taskCount = cacheTaskCount.get(identifier);

            if (taskCount != null) {
                if (taskCount == 1) {
                    cacheTaskCount.remove(identifier);
                } else {
                    cacheTaskCount.put(identifier, taskCount - 1);
                }
            }

            return cache.computeIfAbsent(identifier, absenceMapping);
        });
    }

    /**
     * Notifies the cache about a pending request for stats to better monitor if the cache should flush
     * @param identifier The identifier for the stats
     */
    public void notifyTaskFor(@NotNull I identifier) {
        cacheTaskCount.merge(identifier, 1, Integer::sum);
    }

    /**
     * Accesses part of the cache while waiting for any pending flushes to complete
     * @param identifier The identifier for the member of the cache
     * @param callback The callback for when cache access is ready
     * @return The value returned by the callback
     */
    private <R> R accessCacheMember(@NotNull I identifier, @NotNull Supplier<R> callback) {
        pendingRequestPhaser.register(); // register the request
        waitIfOngoingFlush();

        R value;
        synchronized (getLockFor(identifier)) {
            value = callback.get();
            pendingRequestPhaser.arriveAndDeregister();
        }
        return value;
    }

    /**
     * Accesses part of the cache while waiting for any pending flushes to complete
     * @param identifier The identifier for the member of the cache
     * @param callback The callback for when cache access is ready
     */
    private void accessCacheMember(@NotNull I identifier, @NotNull Runnable callback) {
        pendingRequestPhaser.register(); // register the request
        waitIfOngoingFlush();

        synchronized (getLockFor(identifier)) {
            callback.run();
            pendingRequestPhaser.arriveAndDeregister();
        }
    }

    /**
     * Waits for a flush to complete if it is ongoing
     * Must be called after registering to pendingRequestPhaser
     */
    private void waitIfOngoingFlush() {
        // check for ongoing flush
        if (pendingFlushPhaser.getRegisteredParties() > 0) {
            pendingFlushPhaser.register(); // add request to the parties waiting for the flush
            pendingRequestPhaser.arriveAndDeregister(); // unregister from requests due to needing to wait
            pendingFlushPhaser.arriveAndAwaitAdvance(); // await flush completion
            pendingRequestPhaser.register(); // reregister the request
        }
    }

    /**
     * Get an object lock for an identifier to synchronize on
     * @param identifier The identifier of the lock
     * @return The object lock
     */
    private @NotNull Object getLockFor(@NotNull I identifier) {
        return locks.computeIfAbsent(identifier, (unused) -> new Object());
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

    /**
     * Flushes the cache of any unnecessary values
     * @param callback A callback for each set of flushed stats
     */
    public synchronized void flush(@NotNull Consumer<Stats<I>> callback) {
        pendingFlushPhaser.register(); // register a flush to notify requests to wait
        pendingRequestPhaser.register(); // register to the pending requests
        pendingRequestPhaser.arriveAndAwaitAdvance(); // await until all pending requests have completed

        Iterator<Map.Entry<I, Stats<I>>> iterator = cache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<I, Stats<I>> next = iterator.next();
            callback.accept(next.getValue());

            // Remove item from the cache if there aren't any enqueued tasks that want to modify the stats
            if (!cacheTaskCount.containsKey(next.getKey())) {
                locks.remove(next.getKey());
                iterator.remove();
            }
        }

        pendingFlushPhaser.arriveAndDeregister(); // complete flush and restart waiting requests
    }

}
