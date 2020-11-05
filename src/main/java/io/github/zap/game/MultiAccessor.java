package io.github.zap.game;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic utility class that enables multiple named accessors to access different copies of a single value.
 * @param <T> The kind of value
 */
@RequiredArgsConstructor
public class MultiAccessor<T> {
    private final Map<String, T> mappings = new HashMap<>();
    private final T defaultValue;

    /**
     * Gets the value that is stored for this accessor. Returns the fallback value if it is not in the internal
     * map.
     * @param accessor The accessor name
     * @return The value stored for this accessor, or the default value used by this instance
     */
    public T getValue(String accessor) {
        return mappings.getOrDefault(accessor, defaultValue);
    }

    /**
     * Sets the value associated with this accessor.
     * @param accessor The accessor to use
     * @param value The value to store for this accessor
     */
    public void setValue(String accessor, T value) {
        mappings.put(accessor, value);
    }

    /**
     * Returns whether or not the existing accessor has any stored value associated with it.
     * @param accessor The accessor to look for
     * @return true if the accessor has a value, false if it doesn't
     */
    public boolean hasAccessor(String accessor) {
        return mappings.containsKey(accessor);
    }

    /**
     * Removes an accessor and any stored value from the internal map.
     * @param accessor The accessor to remove
     */
    public void removeAccessor(String accessor) {
        mappings.remove(accessor);
    }
}
