package io.github.zap.arenaapi.serialize;

import com.google.common.collect.Lists;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.LoadFailureException;
import io.github.zap.arenaapi.util.ReflectionUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Implementation of DataLoader for Bukkit's ConfigurationSerializable.
 */
public class BukkitDataLoader implements DataLoader {
    private static final String EXTENSION = "yml";

    @SafeVarargs
    public BukkitDataLoader(Class<? extends DataSerializable>... args) throws LoadFailureException {
        try {
            Field aliases = ConfigurationSerialization.class.getDeclaredField("aliases");
            aliases.setAccessible(true);

            /*
            this map is used by ConfigurationSerialization to resolve class names/aliases into the class containing
            the deserializer method
             */
            @SuppressWarnings("unchecked") Map<String, Class<? extends ConfigurationSerializable>> aliasesMap =
                    (Map<String, Class<? extends ConfigurationSerializable>>) aliases.get(null);

            //map all specified ConfigurationSerializable objects to the same deserializer

            List<Class<? extends DataSerializable>> items = Lists.newArrayList(args);
            items.add(EnumWrapper.class); //global support for enums

            for(Class<? extends DataSerializable> arg : items) {
                String name = arg.getName();
                TypeAlias typeAlias = ReflectionUtils.getDeclaredAnnotation(arg, TypeAlias.class);

                if(typeAlias != null) {
                    String alias = typeAlias.alias();

                    if(!alias.equals(StringUtils.EMPTY)) {
                        name = alias;
                    }
                    else {
                        throw new LoadFailureException(String.format("Invalid alias in class '%s'", name));
                    }
                }

                aliasesMap.put(name, DataSerializable.class);

                try {
                    DataSerializable.registerAlias(name, arg);
                }
                catch (IllegalArgumentException e) { //duplicate value, etc
                    throw new LoadFailureException("IllegalArgumentException occured when registering " +
                            "DataSerializableClass: duplicate values are not permitted");
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            throw new LoadFailureException(e.getMessage());
        }
    }

    @Override
    public <T extends DataSerializable> void save(T data, File file, String name) {
        FileConfiguration configuration = new YamlConfiguration();
        configuration.set(name, data);

        try {
            configuration.save(file);
        } catch (IOException e) {
            ArenaApi.getInstance().getLogger().warning(String.format("IOException when attempting to save data with " +
                    "name '%s' to config file '%s': %s", name, file, e.getMessage()));
        }
    }

    @Override
    public <T extends DataSerializable> T load(File file, String name) {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        try {
            //noinspection unchecked
            return (T) configuration.get(name);
        }
        catch (ClassCastException e) {
            ArenaApi.getInstance().getLogger().warning(String.format("Incompatible type cast applied to named data " +
                    "'%s'", name));
        }

        return null;
    }

    @Override
    public String getExtension() {
        return EXTENSION;
    }
}