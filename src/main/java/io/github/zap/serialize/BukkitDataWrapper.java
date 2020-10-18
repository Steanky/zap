package io.github.zap.serialize;

import io.github.zap.ZombiesPlugin;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BukkitDataWrapper<T extends DataSerializable> extends DataWrapper<T> implements ConfigurationSerializable {
    public BukkitDataWrapper(SerializationProvider serializationProvider, T data) {
        super(serializationProvider, data);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = super.serialize();
        data.put("typeClass", getData().getClass().getTypeName()); //this extra data may not be needed in all implementations
        return data;
    }

    //a method with this exact signature is required by Bukkit's ConfigurationSerializable
    public static BukkitDataWrapper<? extends DataSerializable> deserialize(Map<String, Object> data) {
        return (BukkitDataWrapper<?>)ZombiesPlugin.getInstance().getSerializationProvider().deserialize(data);
    }
}
