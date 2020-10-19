package io.github.zap.serialize;

public interface DataLoader {
    <T extends DataSerializable> void save(T data, String path, String name);
    <T extends DataSerializable> T load(String path, String name);
}
