package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface DataSource {
    @NotNull Optional<DataContainer> readContainer(@NotNull String key);

    void writeContainer(@NotNull Object data, @NotNull String key);

    void registerLoader(@NotNull DataLoader loader, @NotNull String key);

    DataLoader getLoader(@NotNull String key);
}
