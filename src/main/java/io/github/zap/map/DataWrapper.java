package io.github.zap.map;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * General interface for a class that wraps data objects so they can be serialized in a unified manner.
 * @param <T> The type of object to wrap, which must be a serializer of itself
 */
public abstract class DataWrapper<T extends DataSerializer> {
    protected static final Map<String, DataDeserializer<?>> deserializers = new HashMap<>();

    /**
     * Registers a deserializer.
     * @param serializerClass The class to register
     * @param deserializer The deserializer that will deserialize this class
     * @param <T> The type of data object that will be serialized
     */
    public static <T extends DataSerializer> void registerDeserializer(Class<T> serializerClass, DataDeserializer<T> deserializer) {
        Objects.requireNonNull(serializerClass, "serializerClass cannot be null");
        Objects.requireNonNull(deserializer, "deserializer cannot be null");

        deserializers.put(serializerClass.getTypeName(), deserializer);
    }
}
