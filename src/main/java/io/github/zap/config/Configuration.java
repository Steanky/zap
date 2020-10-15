package io.github.zap.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Configuration {
    @Getter
    private final FileConfiguration fileConfiguration;

    private final Map<String, Predicate> validators;

    public Configuration(FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
        validators = new HashMap<>();
    }

    /**
     * Registers a validator for this Configuration object
     * @param name The name of the validator. This should be the same as the path of the config value.
     * @param validator The predicate that will test the value retrieved from the config file.
     */
    public void registerValidator(String name, Predicate validator) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(validator, "validator cannot be null");

        validators.put(name, validator);
    }

    /**
     * Get the value stored in the internal FileConfiguration.
     * @param path The name of the value
     * @param fallback The default value, if the predicate fails. If the value does not exist, null is returned.
     * @param <T> The type of object to retrieve
     * @return The stored object
     */
    public <T> T get(String path, T fallback) {
        T value = (T)fileConfiguration.get(path);
        if(value == null) {
            return null;
        }

        Predicate validator = validators.get(path);
        if(validator != null) {
            if(!validator.test(value)) {
                return fallback;
            }
        }

        return value;
    }

    //TODO: wrap more functions of FileConfiguration
}