package io.github.zap.arenaapi.serialize;

import java.io.File;

public interface DataLoader {
    void save(Object data, String filename);

    <T> T load(String filename, Class<T> objectClass);

    File getRootDirectory();

    File getFile(String name);

    String getExtension();
}
