package io.github.zap.serialize;

import java.util.Map;

public interface SerializationProvider {
    void save(DataSerializable data, String path, String name);
    <T extends DataSerializable> T load(String path, String name);
    <T extends DataSerializable> DataWrapper<T> deserialize(Map<String, Object> data);
    Object wrap(Object data);
    Object unwrap(Object data);
}
