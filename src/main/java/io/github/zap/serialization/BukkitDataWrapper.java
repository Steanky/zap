package io.github.zap.serialization;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;

/**
 * A class that wraps bukkit data objects so they can be serialized.
 */
public class BukkitDataWrapper<T extends DataSerializer> extends DataWrapper<T> implements ConfigurationSerializable {
    public BukkitDataWrapper(T data) {
        super(data);
    }

    /**
     * This function is required by Bukkit's ConfigurationSerializable to deserialize DataWrappers. It cannot have
     * type parameters.
     * @param data The data to deserialize
     * @return Returns a DataWrapper representing the deserialized object
     */
    public static BukkitDataWrapper<? extends DataSerializer> deserialize(Map<String, Object> data) {
        String type = (String) data.get("typeClass");
        DataDeserializer<? extends DataSerializer> deserializer = deserializers.get(type);

        return new BukkitDataWrapper<>(deserializer.deserialize(data));
    }
}
