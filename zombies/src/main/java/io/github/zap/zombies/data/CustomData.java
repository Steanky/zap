package io.github.zap.zombies.data;

import io.github.zap.arenaapi.Unique;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an object that can contain custom data that pertains to specific objects
 */
public class CustomData implements Unique {

    private final Map<String, String> customData = new HashMap<>();

    @Getter
    private final transient UUID id = UUID.randomUUID();

    /**
     * Gets a custom data value from the custom data map
     * @param key The key of the data value
     * @return The data value
     */
    public String getCustomData(String key) {
        return customData.get(key);
    }

}
