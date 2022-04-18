package io.github.zap.arenaapi.stats;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.serialize.DataLoader;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

/**
 * Stats manager which stores stats using the file system
 */
public class FileStatsManager extends StatsManager {

    private final Map<String, DataLoader> dataLoaderMap;

    public FileStatsManager(@NotNull Plugin plugin, @NotNull ExecutorService executorService,
                            @NotNull Map<String, DataLoader> dataLoaderMap) {
        super(plugin, executorService);

        this.dataLoaderMap = dataLoaderMap;
    }

    @Override
    protected <I, S extends Stats<I>> @NotNull S loadStats(@NotNull String cacheName, @NotNull I identifier,
                                                           @NotNull Class<S> statsClass,
                                                           @NotNull Function<I, S> callback) {
        DataLoader dataLoader = dataLoaderMap.get(cacheName);
        if (dataLoader != null) {
            File file = dataLoader.getFile(identifier.toString());
            if (file != null && file.exists()) {
                S stats = dataLoader.load(identifier.toString(), statsClass);
                if (stats != null) {
                    return stats;
                }
            }
        }

        return callback.apply(identifier);
    }

    @Override
    protected <I, S extends Stats<I>> void writeStats(@NotNull String cacheName, @NotNull S stats) {
        DataLoader dataLoader = dataLoaderMap.get(cacheName);
        if (dataLoader != null) {
            dataLoader.save(stats, stats.getIdentifier().toString());
        } else {
            ArenaApi.warning("Tried to write stats for cache with name " + cacheName + " for which no data loader " +
                    "exists, skipping write");
        }
    }

}
