package io.github.zap.zombies.game.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an object that can contain custom data that pertains to specific objects
 */
public class CustomData {

    private final Map<String, String> customData = new HashMap<>();
    /**
     * Gets a custom data value from the custom data map
     * @param key The key of the data value
     * @return The data value
     */
    public String getCustomData(String key) {
        return customData.get(key);
    }

}
