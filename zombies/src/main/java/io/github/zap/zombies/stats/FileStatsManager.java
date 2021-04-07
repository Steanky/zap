package io.github.zap.zombies.stats;

import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.zombies.stats.player.PlayerGeneralStats;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@RequiredArgsConstructor
public class FileStatsManager extends StatsManager {

    private final DataLoader dataLoader;

    @Override
    protected @NotNull PlayerGeneralStats loadPlayerStatsFor(@NotNull UUID uuid) {
        String uuidString = uuid.toString();

        return (dataLoader.getFile(uuidString).exists())
                ? dataLoader.load(uuid.toString(), PlayerGeneralStats.class)
                : new PlayerGeneralStats(uuid);
    }

    @Override
    protected void writePlayerStats(@NotNull PlayerGeneralStats stats) {
        dataLoader.save(stats, stats.getUuid().toString());
    }

}
