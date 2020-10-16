package io.github.zap.map;

import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * A class that wraps bukkit data objects so they can be serialized.
 */
public class BukkitDataWrapper<T extends DataSerializer> extends DataWrapper<T> implements ConfigurationSerializable {
    public BukkitDataWrapper(T data) {
        super(data);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = getData().serialize();
        result.put("typeClass", getData().getClass().getTypeName());

        return result;
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