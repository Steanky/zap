package io.github.zap.map;

import java.util.Map;

/**
 * General interface for data classes that can be serialized.
 * @param <T> The type of object that is serialized
 */
public interface DataSerializer {
    /**
     * Get a map representing the object's serialized form.
     * @return A map representing the object's fields
     */
    Map<String, Object> serialize();
}