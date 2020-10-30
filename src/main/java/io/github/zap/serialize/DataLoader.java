package io.github.zap.serialize;

public interface DataLoader {
    /**
     * Saves the specified serializable data to the provided path, under the given name.
     * @param data The data to save
     * @param path The location to save to
     * @param name The name of the data (not necessarily used by all implementations)
     * @param <T> The type of data to save
     */
    <T extends DataSerializable> void save(T data, String path, String name);

    /**
     * Loads serializable data from the provided path, under the given name.
     * @param path The path to load from
     * @param name The name of the data to retrieve
     * @param <T> The type of the data
     * @return The data itself
     */
    <T extends DataSerializable> T load(String path, String name);
}
