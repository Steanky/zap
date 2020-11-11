package io.github.zap.serialize;

import io.github.zap.ZombiesPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

public class BukkitDataLoader implements DataLoader {
    @SuppressWarnings("unchecked")
    public BukkitDataLoader(Class<? extends ConfigurationSerializable>... args) {
        ConfigurationSerialization.registerClass(DataSerializable.class);

        try {
            Field aliases = ConfigurationSerialization.class.getDeclaredField("aliases");
            aliases.setAccessible(true);

            /*
            this map is used by ConfigurationSerialization to resolve class names/aliases into the class containing
            the deserializer method
             */
            Map<String, Class<? extends ConfigurationSerializable>> aliasesMap =
                    (Map<String, Class<? extends ConfigurationSerializable>>) aliases.get(null);

            //map all specified ConfigurationSerializable objects to the same deserializer
            for(Class<? extends ConfigurationSerializable> arg : args) {
                aliasesMap.put(arg.getName(), DataSerializable.class);
            }

            //below classes are always compatible with DataSerializable
            aliasesMap.put(EnumWrapper.class.getName(), DataSerializable.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(String.format("Exception when editing ConfigurationSerialization aliases " +
                    "map: %s", e.getMessage()));
        }
    }

    @Override
    public <T extends DataSerializable> void save(T data, File file, String name) {
        FileConfiguration configuration = new YamlConfiguration();
        configuration.set(name, data);

        try {
            configuration.save(file);
        } catch (IOException ignored) {
            ZombiesPlugin.getInstance().getLogger().warning(String.format("IOException when attempting to save to " +
                    "config file '%s'", file));
        }
    }

    @Override
    public <T extends DataSerializable> T load(File file, String name) {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        //noinspection unchecked
        return (T) configuration.get(name);
    }
}