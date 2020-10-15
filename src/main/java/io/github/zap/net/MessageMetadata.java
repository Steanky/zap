package io.github.zap.net;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that holds extra parameters for plugin messages. Will possibly replace with some sort of immutable map later
 * since that's basically what this is.
 */
public class MessageMetadata {
    private final Map<String, String> data = new HashMap<>();

    /**
     * Adds a name/value pair to the internal metadata map
     * @param name The name of the value
     * @param value The value
     */
    public void addData(String name, String value) {
        data.put(name, value);
    }

    /**
     * Gets a named string from the internal map
     * @param name The name of the string to retrieve
     * @return The string itself
     */
    public String getData(String name) {
        return data.get(name);
    }

    /**
     * Attempts to fetch the data associated with the provided name, returning a fallback string if it does not succeed
     * @param name The name of the data to retrieve
     * @param fallback The data to return, if it does not exist in the backing map
     * @return The data stored in the backing map or the fallback string
     */
    public String getDataOrDefault(String name, String fallback) {
        return data.getOrDefault(name, fallback);
    }

    /**
     * Tests whether or not the provided name is present in the internal map.
     * @param name The name
     * @return Whether or not the data is present in the backing map
     */
    public boolean hasData(String name) {
        return data.containsKey(name);
    }
}
