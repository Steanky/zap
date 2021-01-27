package io.github.zap.arenaapi.serialize;

import java.io.File;

public interface DataLoader {
    /**
     * Saves the specified data to the provided filename. The file extension will be added by the DataLoader
     * implementation. Files and subdirectories will be specified relative to the root directory for this loader,
     * which is the File returned by getRootDirectory().
     * @param data The data to save
     * @param filename The filename for the data
     */
    void save(Object data, String filename);

    /**
     * Loads the specified data given the provided filename, which should not include an extension. The filename should
     * be relative to the root directory for this loader.
     * @param filename The name of the file (without an extension)
     * @param objectClass The class of object we're loading
     * @param <T> The type of object we're loading
     * @return The loaded object
     */
    <T> T load(String filename, Class<T> objectClass);

    /**
     * Gets the root directory for this loader, from which all files/paths are relative to.
     * @return The root directory for this loader
     */
    File getRootDirectory();

    /**
     * Gets a file relative to the root directory for this loader.
     * @param name The relative filename
     * @return The file
     */
    File getFile(String name);

    /**
     * Gets the extension used for this loader.
     * @return The extension
     */
    String getExtension();
}
