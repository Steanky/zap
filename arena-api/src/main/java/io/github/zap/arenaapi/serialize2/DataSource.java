package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface DataSource {
    @NotNull Optional<DataContainer> pullContainer(@NotNull String key);

    void pushContainer(@NotNull DataContainer container, @NotNull String key);

    void associateLoader(@NotNull DataLoader loader, @NotNull String key);

    static @NotNull DataSource newStandard() {
        return new StandardDataSource();
    }
}
