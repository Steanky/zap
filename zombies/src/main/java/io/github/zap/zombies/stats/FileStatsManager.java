package io.github.zap.zombies.stats;

import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.zombies.stats.player.PlayerGeneralStats;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@RequiredArgsConstructor
public class FileStatsManager extends StatsManager {

    private final DataLoader dataLoader;

    @Override
    protected @Nullable
    PlayerGeneralStats loadPlayerStatsFor(@NotNull UUID uuid) {
        return dataLoader.load(uuid.toString(), PlayerGeneralStats.class);
    }

    @Override
    protected void writePlayerStats(@NotNull PlayerGeneralStats stats) {
        dataLoader.save(stats, stats.getUuid().toString());
    }

}
