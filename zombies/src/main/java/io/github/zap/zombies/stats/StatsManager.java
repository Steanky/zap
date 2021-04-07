package io.github.zap.zombies.stats;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.stats.player.PlayerGeneralStats;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class StatsManager implements Disposable {

    private final static int MAXIMUM_CACHE_SIZE = 50;

    private final Map<UUID, PlayerGeneralStats> playerCache = new HashMap<>();

    private final Map<UUID, Integer> playerTaskCountMap = new ConcurrentHashMap<>();

    private final ExecutorService playerExecutorService = Executors.newSingleThreadExecutor();

    /**
     * Enqueues a task to modify player stats
     * @param targetPlayer The player whose stats should be modified
     * @param task The task to modify the player stats
     */
    public void modifyStatsForPlayer(@NotNull OfflinePlayer targetPlayer, @NotNull Consumer<PlayerGeneralStats> task) {
        UUID uuid = targetPlayer.getUniqueId();
        playerTaskCountMap.merge(uuid, 1, Integer::sum);

        playerExecutorService.submit(() -> {
            PlayerGeneralStats stats = playerCache.computeIfAbsent(uuid, this::loadPlayerStatsFor);
            task.accept(stats);

            int currentTaskCount = playerTaskCountMap.get(uuid);
            if (currentTaskCount == 1) {
                playerTaskCountMap.remove(uuid);
            } else {
                playerTaskCountMap.put(uuid, currentTaskCount - 1);
            }
        });

        // This cache size check does not need to be threadsafe since it is only approximate
        if (playerCache.size() > MAXIMUM_CACHE_SIZE) {
            flushCache();
        }
    }

    /**
     * Flushes the cache of player stats and writes them to their storage location
     */
    public void flushCache() {
        playerExecutorService.submit(() -> {
            Iterator<Map.Entry<UUID, PlayerGeneralStats>> iterator = playerCache.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, PlayerGeneralStats> next = iterator.next();
                writePlayerStats(next.getValue());

                // Remove item from the cache if there aren't any enqueued tasks that want to modify the player stats
                if (!playerTaskCountMap.containsKey(next.getKey())) {
                    iterator.remove();
                }
            }
        });
    }

    @Override
    public void dispose() {
        flushCache();
        playerExecutorService.shutdown();
        try {
            //noinspection ResultOfMethodCallIgnored
            playerExecutorService.awaitTermination(69L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Zombies.warning("ExecutorService interrupted while flushing StatsManager cache!");
        }
    }

    /**
     * Loads a player's stats
     * @param uuid The uuid of the player whose stats should be loaded
     * @return The player's stats
     */
    protected abstract @NotNull PlayerGeneralStats loadPlayerStatsFor(@NotNull UUID uuid);

    /**
     * Writes a player's stats to storage
     * @param stats The stats to write
     */
    protected abstract void writePlayerStats(@NotNull PlayerGeneralStats stats);

}
