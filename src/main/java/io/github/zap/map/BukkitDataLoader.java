package io.github.zap.map;

import io.github.zap.ZombiesPlugin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.Objects;

/**
 * This implementation of DataLoader uses Bukkit's serialization framework, which saves data in YAML files.
 */
public class BukkitDataLoader implements DataLoader {
    @Override
    public <T extends DataSerializer> void save(T data, String relativePath, String name) {
        FileConfiguration config = new YamlConfiguration();
        config.set(name, new BukkitDataWrapper<>(data));

        try {
            config.save(relativePath);
        } catch (IOException e) {
            ZombiesPlugin.getInstance().getLogger().warning(String.format("Failed to save to configuration file %s: %s", relativePath, e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataSerializer> T load(String relativePath, String name) {
        FileConfiguration config = new YamlConfiguration();

        try {
            config.load(relativePath);
        } catch (IOException | InvalidConfigurationException e) {
            ZombiesPlugin.getInstance().getLogger().warning(String.format("Failed to load from configuration file %s: %s", relativePath, e.getMessage()));
            return null;
        }

        BukkitDataWrapper<T> wrapped = (BukkitDataWrapper<T>)config.get(name);
        Objects.requireNonNull(wrapped, String.format("data named %s does not exist in config file %s", name, relativePath));

        return wrapped.getData();
    }
}
