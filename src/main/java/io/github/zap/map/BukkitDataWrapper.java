package io.github.zap.map;

import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A class that wraps bukkit data objects so they can be serialized.
 */
public class BukkitDataWrapper<T extends DataSerializer<T>> implements DataWrapper<T>, ConfigurationSerializable {
    private static final Map<String, DataDeserializer<?>> deserializers = new HashMap<>();

    @Getter
    private final DataSerializer<T> dataSerializer;

    public BukkitDataWrapper(DataSerializer<T> dataSerializer) {
        this.dataSerializer = dataSerializer;
    }

    /**
     * Gets the underlying data object associated with this DataWrapper.
     * @return The underlying data object
     */
    @SuppressWarnings("unchecked")
    @Override
    public T getDataObject() {
        return (T)dataSerializer;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = dataSerializer.serialize();
        result.put("typeClass", dataSerializer.getClass().getTypeName());

        return result;
    }

    /**
     * Registers a deserializer.
     * @param serializerClass The class to register
     * @param deserializer The deserializer that will deserialize this class
     * @param <T> The type of data object that will be serialized
     */
    public static <T extends DataSerializer<T>> void registerDeserializer(Class<T> serializerClass, DataDeserializer<T> deserializer) {
        Objects.requireNonNull(serializerClass, "serializerClass cannot be null");
        Objects.requireNonNull(deserializer, "deserializer cannot be null");

        deserializers.put(serializerClass.getTypeName(), deserializer);
    }

    /**
     * This function is required by Bukkit's ConfigurationSerializable to deserialize DataWrappers. It cannot have
     * type parameters.
     * @param data The data to deserialize
     * @return Returns a DataWrapper representing the deserialized object
     */
    @SuppressWarnings({"unused", "unchecked", "RedundantSuppression", "rawtypes", "SuspiciousMethodCalls"})
    public static BukkitDataWrapper deserialize(Map<String, Object> data) {
        Object type = data.get("typeClass");
        DataDeserializer<?> deserializer = deserializers.get(type);

        return new BukkitDataWrapper(deserializer.deserialize(data));
    }
}