package io.github.zap.serialize;

import java.io.File;

public interface DataLoader {
    /**
     * Saves the specified serializable data to the provided path, under the given name.
     * @param data The data to save
     * @param file The file to save to
     * @param name The name of the data (not necessarily used by all implementations)
     * @param <T> The type of data to save
     */
    <T extends DataSerializable> void save(T data, File file, String name);

    /**
     * Loads serializable data from the provided path, under the given name.
     * @param file The file to load from
     * @param name The name of the data to retrieve (not used by all implementations)
     * @param <T> The type of the data
     * @return The data itself
     */
    <T extends DataSerializable> T load(File file, String name);
}
