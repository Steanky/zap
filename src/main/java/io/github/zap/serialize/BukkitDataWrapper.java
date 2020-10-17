package io.github.zap.serialize;

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

    /**
     * Serializes the data class.
     * @return The serialized data
     */
    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> result = getData().serialize();

        for(String key : result.keySet()) { //wrap objects
            Object object = result.get(key);

            if(object instanceof DataSerializer) {
                result.put(key, new BukkitDataWrapper<>((DataSerializer)object));
            }
        }

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

        for(String key : data.keySet()) { //unwrap objects
            Object object = data.get(key);
            if(object instanceof DataWrapper) {
                DataWrapper<?> wrapper = (DataWrapper<?>)object;
                data.put(key, wrapper.getData());
            }
        }

        return new BukkitDataWrapper<>(deserializer.deserialize(data));
    }
}