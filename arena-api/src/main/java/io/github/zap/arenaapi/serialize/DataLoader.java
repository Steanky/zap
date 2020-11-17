package io.github.zap.arenaapi.serialize;

import java.io.File;

/**
 * This represents a class capable of saving and loading serialized data using a specific implementation (JSON, YAML,
 * XML, etc).
 */
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

    /**
     * Gets the filename extension that this loader saves to/loads from.
     * @return The filename extension used by this loader, without the leading period
     */
    String getExtension();
}
