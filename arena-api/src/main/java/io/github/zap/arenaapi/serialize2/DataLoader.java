package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface DataLoader {
    @NotNull Optional<DataContainer> read();

    void write(@NotNull DataContainer container);

    @NotNull ContainerFactory factory();
}
