package io.github.zap.serialization;

import java.util.Map;

/**
 * General functional interface for deserializing a map into a data object.
 * @param <T> The type of data object this deserializer outputs
 */
public interface DataDeserializer<T extends DataSerializer> {
    /**
     * Deserializes the provided map into a data object
     * @param data The map to deserialize
     * @return The data object
     */
    T deserialize(Map<String, Object> data);
}
