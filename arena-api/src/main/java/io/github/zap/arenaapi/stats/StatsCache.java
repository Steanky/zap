package io.github.zap.arenaapi.stats;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * Represents a cache of stats
 * @param <I> The identifier of the stats
 * @param <S> The type of the stats
 */
// TODO: proper failure handling, currently stuff will just lock up if statsmanager doesn't shutdown correctly
public class StatsCache<I, S extends Stats<I>> {

    private final Plugin plugin;

    private final String name;

    private final Class<S> statsClass;

    private final int maximumFreeCacheSize;

    private final Map<I, S> cache = new ConcurrentHashMap<>();

    private final Map<I, Integer> cacheTaskCount = new ConcurrentHashMap<>();

    private final Map<I, Object> locks = new ConcurrentHashMap<>();

    private final Phaser pendingFlushPhaser = new Phaser(), pendingRequestPhaser = new Phaser();

    private final ReentrantLock flushLock = new ReentrantLock();

    private final Map<UUID, ConcurrentHashMap<Integer, Object>> map = new ConcurrentHashMap<>();

    public StatsCache(@NotNull Plugin plugin, @NotNull String name, @NotNull Class<S> statsClass,
                      int maximumFreeCacheSize) {
        this.plugin = plugin;
        this.name = name;
        this.statsClass = statsClass;
        this.maximumFreeCacheSize = maximumFreeCacheSize;
    }

    /**
     * Gets a value in the cache. Note that the callback is supposed to be called on the same thread.
     * @param identifier The identifier for the value
     * @param absenceMapping The mapping to determine the value if it is not present in the cache
     * @param callback The callback that accepts the cache value
     */
    public void getValueFor(@NotNull I identifier, @NotNull Function<I, S> absenceMapping,
                            @NotNull Consumer<S> callback) {
        accessCacheMember(identifier, () -> {
            Integer taskCount = cacheTaskCount.get(identifier);

            if (taskCount != null) {
                if (taskCount == 1) {
                    cacheTaskCount.remove(identifier);
                } else {
                    cacheTaskCount.put(identifier, taskCount - 1);
                }
            }

            S stats = cache.computeIfAbsent(identifier, absenceMapping);
            try {
                callback.accept(stats);
            } catch (Exception exception) {
                plugin.getLogger().log(Level.WARNING, "A stat modification caused an exception.", exception);
            }
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
     */
    private void accessCacheMember(@NotNull I identifier, @NotNull Runnable callback) {
        synchronized (pendingRequestPhaser) {
            pendingRequestPhaser.register();
        }
        waitIfOngoingFlush();

        synchronized (getLockFor(identifier)) { // Make sure checks for ongoing requests are accurate
            callback.run();
            synchronized (pendingRequestPhaser) { // Make sure checks for ongoing requests are accurate
                pendingRequestPhaser.arriveAndDeregister();
            }
        }
    }

    /**
     * Waits for a flush to complete if it is ongoing
     * Must be called after registering to pendingRequestPhaser
     */
    private void waitIfOngoingFlush() {
        Integer phase = null;
        synchronized (pendingRequestPhaser) { // Make sure that the current requests do not complete or else we arrive on an incorrect phase
            synchronized (pendingFlushPhaser) { // Make sure flush phase does not update
                if (pendingFlushPhaser.getRegisteredParties() > 0) {
                    phase = pendingFlushPhaser.getPhase(); // Set phase if ongoing flush
                    pendingRequestPhaser.arrive(); // "Disable" request until flush done
                }
            }
        }
        if (phase != null) {
            pendingFlushPhaser.awaitAdvance(phase); // await flush completion
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
     * @return Whether the cache should be flushed
     */
    public boolean shouldFlush() {
        return cache.size() - cacheTaskCount.size() > maximumFreeCacheSize
                && pendingFlushPhaser.getRegisteredParties() <= 0;
    }

    /**
     * Flushes the cache of any unnecessary values
     * Do <b>not</b> call this in the callback of {@link #getValueFor(Object, Function, Consumer)}, as it will cause
     * a deadlock.
     * @param callback A callback for each set of flushed stats
     */
    public void flush(@NotNull Consumer<Stats<I>> callback) {
        if (flushLock.tryLock()) { // If the lock is not acquired, another flush is already happening so no reason to flush
            try {
                synchronized (pendingFlushPhaser) { // Make sure checks for flushes are accurate
                    pendingFlushPhaser.register(); // Register a flush to notify requests to wait
                }
                waitIfOngoingRequests();

                Iterator<Map.Entry<I, S>> iterator = cache.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<I, S> next = iterator.next();
                    callback.accept(next.getValue());

                    // Remove item from the cache if there aren't any enqueued tasks that want to modify the stats
                    if (!cacheTaskCount.containsKey(next.getKey())) {
                        locks.remove(next.getKey());
                        iterator.remove();
                    }
                }

                synchronized (pendingFlushPhaser) { // Make sure checks for flushes are accurate
                    pendingFlushPhaser.arriveAndDeregister(); // complete flush and restart waiting requests
                }
            } finally {
                flushLock.unlock();
            }
        }
    }

    /**
     * Used by {@link #flush(Consumer)} to wait for all ongoing requests to complete before continuing.
     */
    private void waitIfOngoingRequests() {
        Integer phase = null;
        synchronized (pendingRequestPhaser) { // Make sure request phase does not update
            if (pendingRequestPhaser.getRegisteredParties() > 0) { // Check for ongoing requests
                phase = pendingRequestPhaser.getPhase(); // Set phase if ongoing requests in order to wait
            }
        }
        if (phase != null) {
            pendingRequestPhaser.awaitAdvance(phase);
        }
    }

    /**
     * Gets the name of the cache
     * @return The cache name
     */
    public @NotNull String getName() {
        return this.name;
    }

    /**
     * Gets the class associated with the stats
     * @return The class
     */
    public @NotNull Class<S> getStatsClass() {
        return this.statsClass;
    }

}
