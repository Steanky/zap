package io.github.zap.zombies.stats;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.stats.map.MapStats;
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

/**
 * Manages Zombies game stats
 */
public abstract class StatsManager implements Disposable {

    private final static int MAXIMUM_FREE_PLAYER_CACHE_SIZE = 10;

    private final static int MAXIMUM_FREE_MAP_CACHE_SIZE = 3;

    private static final ExecutorService PLAYER_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    private final Map<UUID, PlayerGeneralStats> playerCache = new HashMap<>();

    private final Map<UUID, Integer> playerTaskCountMap = new ConcurrentHashMap<>();

    private final Map<String, MapStats> mapCache = new HashMap<>();

    private final Map<String, Integer> mapTaskCountMap = new ConcurrentHashMap<>();

    private static final ExecutorService mapExecutorService = Executors.newSingleThreadExecutor();

    /**
     * Enqueues a task to modify player stats
     * @param targetPlayer The player whose stats should be modified
     * @param task The task to modify the player stats
     */
    public void modifyStatsForPlayer(@NotNull OfflinePlayer targetPlayer, @NotNull Consumer<PlayerGeneralStats> task) {
        UUID uuid = targetPlayer.getUniqueId();
        playerTaskCountMap.merge(uuid, 1, Integer::sum);

        getStatsForPlayer(uuid, (stats) -> {
            task.accept(stats);

            int currentTaskCount = playerTaskCountMap.get(uuid);
            if (currentTaskCount == 1) {
                playerTaskCountMap.remove(uuid);
            } else {
                playerTaskCountMap.put(uuid, currentTaskCount - 1);
            }
        }, false);

        // This cache size check does not need to be threadsafe since it is only approximate
        if (playerCache.size() - playerTaskCountMap.size() > MAXIMUM_FREE_PLAYER_CACHE_SIZE) {
            flushPlayerCache();
        }
    }

    /**
     * Enqueues a task to modify map stats
     * @param map The map whose stats should be modified
     * @param task The task to modify the map stats
     */
    public void modifyStatsForMap(@NotNull MapData map, @NotNull Consumer<MapStats> task) {
        String name = map.getName();
        mapTaskCountMap.merge(name, 1, Integer::sum);

        getStatsForMap(name, (stats) -> {
            task.accept(stats);

            int currentTaskCount = mapTaskCountMap.get(name);
            if (currentTaskCount == 1) {
                mapTaskCountMap.remove(name);
            } else {
                mapTaskCountMap.put(name, currentTaskCount - 1);
            }
        }, false);

        // This cache size check does not need to be threadsafe since it is only approximate
        if (mapCache.size() - mapTaskCountMap.size() > MAXIMUM_FREE_MAP_CACHE_SIZE) {
            flushMapCache();
        }
    }

    /**
     * Requests statistics for a player
     * @param uuid The uuid of the player to request statistics for
     * @param callback The callback for when statistics are retrieved
     * @param urgent Whether the request is urgent and can accept outdated data
     */
    public void getStatsForPlayer(@NotNull UUID uuid, @NotNull Consumer<PlayerGeneralStats> callback,
                                  boolean urgent) {
        if (urgent) {
            PlayerGeneralStats stats = playerCache.get(uuid);

            if (stats != null) {
                callback.accept(stats);
                return;
            }
        }

        PLAYER_EXECUTOR_SERVICE.submit(() -> {
           PlayerGeneralStats stats = playerCache.computeIfAbsent(uuid, this::loadPlayerStatsForPlayer);
           callback.accept(stats);
        });
    }

    /**
     * Requests statistics for a map
     * @param mapName The name of the map to request statistics for
     * @param callback The callback for when statistics are retrieved
     * @param urgent Whether the request is urgent and can accept outdated data
     */
    public void getStatsForMap(@NotNull String mapName, @NotNull Consumer<MapStats> callback, boolean urgent) {
        if (urgent) {
            MapStats stats = mapCache.get(mapName);

            if (stats != null) {
                callback.accept(stats);
                return;
            }
        }

        mapExecutorService.submit(() -> {
            MapStats stats = mapCache.computeIfAbsent(mapName, this::loadMapStatsForMap);
            callback.accept(stats);
        });
    }

    /**
     * Flushes the cache of player stats and writes them to storage
     */
    public void flushPlayerCache() {
        PLAYER_EXECUTOR_SERVICE.submit(() -> {
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

    /**
     * Flushes the cache of map stats and writes them to storage
     */
    public void flushMapCache() {
        mapExecutorService.submit(() -> {
            Iterator<Map.Entry<String, MapStats>> iterator = mapCache.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, MapStats> next = iterator.next();
                writeMapStats(next.getValue());

                // Remove item from the cache if there aren't any enqueued tasks that want to modify the map stats
                if (!mapTaskCountMap.containsKey(next.getKey())) {
                    iterator.remove();
                }
            }
        });
    }

    /**
     * Flushes all caches and writes them to storage
     */
    public void flushCaches() {
        flushPlayerCache();
        flushMapCache();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void dispose() {
        flushCaches();

        PLAYER_EXECUTOR_SERVICE.shutdown();
        mapExecutorService.shutdown();
        try {
            // not quite magic numbers
            PLAYER_EXECUTOR_SERVICE.awaitTermination(69L, TimeUnit.SECONDS);
            mapExecutorService.awaitTermination(420L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Zombies.warning("ExecutorService interrupted while flushing StatsManager cache!");
        }
    }

    /**
     * Loads a player's stats
     * @param uuid The uuid of the player whose stats should be loaded
     * @return The player's stats
     */
    protected abstract @NotNull PlayerGeneralStats loadPlayerStatsForPlayer(@NotNull UUID uuid);

    /**
     * Writes a player's stats to storage
     * @param stats The stats to write
     */
    protected abstract void writePlayerStats(@NotNull PlayerGeneralStats stats);

    /**
     * Loads a map's stats
     * @param mapName The name of the map whose stats should be loaded
     * @return The map's stats
     */
    protected abstract @NotNull MapStats loadMapStatsForMap(@NotNull String mapName);

    /**
     * Writes a map's stats to storage
     * @param stats The stats to write
     */
    protected abstract void writeMapStats(@NotNull MapStats stats);

}
