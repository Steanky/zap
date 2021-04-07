package io.github.zap.zombies.stats;

import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.zombies.stats.game.ZombiesPlayerStats;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@RequiredArgsConstructor
public class FileStatsManager extends StatsManager {

    private final DataLoader dataLoader;

    @Override
    protected @Nullable ZombiesPlayerStats getStatsFor(@NotNull UUID uuid) {
        return dataLoader.load(uuid.toString(), ZombiesPlayerStats.class);
    }

    @Override
    protected void writeStats(@NotNull ZombiesPlayerStats stats) {
        dataLoader.save(stats, stats.getUuid().toString());
    }

}
