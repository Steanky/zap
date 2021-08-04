package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Represents an object that may read and write arbitrary data from DataContainers
 */
public interface DataLoader {
    /**
     * Reads the DataContainer from this loader. If the operation fails (due to IO related or other problems) the
     * returned Optional will be empty and an error may be logged.
     * @return An Optional object containing the DataContainer that was read; may be empty if an error occurred
     */
    @NotNull Optional<DataContainer> read();

    /**
     * Writes the given DataContainer to its source. An error may be logged if the operation failed.
     * @param container The container to write
     */
    void write(@NotNull DataContainer container);

    /**
     * Returns the ContainerFactory used to construct DataContainer instances from arbitrary data.
     * @return A ContainerFactory instance
     */
    @NotNull ContainerFactory factory();
}
