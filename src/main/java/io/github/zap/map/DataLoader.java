package io.github.zap.map;

/**
 * Specifies a higher-level API that manages DataWrapper serialization
 */
public interface DataLoader {
    /**
     * Saves the specified data to the given relative path, potentially using the given name.
     * @param data The data to save
     * @param relativePath The relative path to save to
     * @param name The name of the data, which may not used by all implementations
     */
    <T extends DataSerializer> void save(T data, String relativePath, String name);

    /**
     * Loads a data object from the configuration file at the given path and with the given name.
     * @param relativePath The relative path of the configuration file
     * @param name The name of the data, which may not be used in all implementations
     * @param <T> The type of the data object, which must implement DataSerializer
     * @return The data object
     */
    <T extends DataSerializer> T load(String relativePath, String name);
}
