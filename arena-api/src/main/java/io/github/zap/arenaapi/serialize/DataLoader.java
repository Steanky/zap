package io.github.zap.arenaapi.serialize;

import java.io.File;

public interface DataLoader {
    void save(Object data, File file);

    <T> T load(File file, Class<T> objectClass);

    String getExtension();
}
