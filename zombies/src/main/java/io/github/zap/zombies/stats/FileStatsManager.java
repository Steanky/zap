package io.github.zap.zombies.stats;

import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.zombies.stats.map.MapStats;
import io.github.zap.zombies.stats.player.PlayerGeneralStats;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Manages stats using filesystem
 */
@RequiredArgsConstructor
public class FileStatsManager extends StatsManager {

    private final DataLoader playerDataLoader, mapDataLoader;

    @Override
    protected @NotNull PlayerGeneralStats loadPlayerStatsForPlayer(@NotNull UUID uuid) {
        String uuidString = uuid.toString();

        return (playerDataLoader.getFile(uuidString).exists())
                ? playerDataLoader.load(uuidString, PlayerGeneralStats.class)
                : new PlayerGeneralStats(uuid);
    }

    @Override
    protected void writePlayerStats(@NotNull PlayerGeneralStats stats) {
        playerDataLoader.save(stats, stats.getUuid().toString());
    }

    @Override
    protected @NotNull MapStats loadMapStatsForMap(@NotNull String mapName) {
        return (mapDataLoader.getFile(mapName).exists())
                ? mapDataLoader.load(mapName, MapStats.class)
                : new MapStats(mapName);
    }

    @Override
    protected void writeMapStats(@NotNull MapStats stats) {
        mapDataLoader.save(stats, stats.getMapName());
    }

}
