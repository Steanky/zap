package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Represents a source of DataContainers, which may be read via keys. DataSource implementations should support
 * diverse types of DataLoaders; they should not be specific to any one serialization method.
 */
public interface DataSource {
    /**
     * Reads a container from a DataSource.
     * @param key The key used to access the DataLoader from which the container will be read
     * @return An Optional, which will be empty if an error occurred during reading or if no DataLoader has been
     * registered for the given key
     */
    @NotNull Optional<? extends DataContainer> readContainer(@NotNull String key);

    /**
     * Writes some data to a DataSource. If the requested source does not exist, this function will perform no action.
     * If the requested source exists and there is an IO-related exception or other uncontrollable condition, an error
     * will be logged and no action will be performed.
     * @param data The data to write
     * @param key The name of the DataSource we're writing to
     */
    void writeObject(@NotNull Object data, @NotNull String key);

    /**
     * Associates and stores the given DataLoader with a named key.
     * @param loader The DataLoader to store
     * @param key The name of the DataLoader. If the loader already exists, it will be overwritten
     */
    void registerLoader(@NotNull DataLoader<? extends DataContainer> loader, @NotNull String key);

    /**
     * Gets the DataLoader stored under a key.
     * @param key The name of the DataLoader to retrieve
     * @return The associated DataLoader, or null if none exists
     */
    DataLoader<? extends DataContainer> getLoader(@NotNull String key);

    /**
     * Creates a new standard implementation of DataSource, with a standard transformation context
     * @return A new, standard implementation of DataSource
     */
    static @NotNull DataSource newStandard() {
        return new StandardDataSource();
    }
}
