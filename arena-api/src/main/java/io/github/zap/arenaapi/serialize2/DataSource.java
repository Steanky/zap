package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Represents an object that translates DataKeys into mappings, which may be used to form DataContainers.
 */
public interface DataSource {
    @NotNull Optional<DataContainer> pullContainer(@NotNull DataKey key);

    void pushContainer(@NotNull DataContainer container, @NotNull DataKey key);

    void associateLoader(@NotNull DataLoader loader, @NotNull DataKey key);
}
