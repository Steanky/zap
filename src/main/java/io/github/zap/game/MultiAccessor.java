package io.github.zap.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic utility class that enables different named accessor objects to access their own unique copies of a single
 * value.
 * @param <T> The type of value this accessor stores
 */
@RequiredArgsConstructor
public class MultiAccessor<T> {
    private final Map<String, T> mappings = new HashMap<>();

    @Getter
    private final T defaultValue;

    /**
     * Gets the value that is stored for this accessor. Returns the default value if it is not in the internal map.
     * @param accessor The accessor name
     * @return The value stored for this accessor, or the default value used by this instance
     */
    public T getValue(Named accessor) {
        return mappings.getOrDefault(accessor.getName(), defaultValue);
    }

    /**
     * Sets the value associated with this accessor.
     * @param accessor The accessor to use
     * @param value The value to store for this accessor
     * @return true if the value changed as a result of this call, false otherwise
     */
    public boolean setValue(Named accessor, T value) {
        String name = accessor.getName();
        T oldValue = mappings.get(name);

        if(!value.equals(oldValue)) {
            mappings.put(name, value);
            AccessorManager.getInstance().addAccessor(accessor, this);
            return true;
        }

        return false;
    }

    /**
     * Returns whether or not the existing accessor has any non-default stored value associated with it.
     * @param accessor The accessor to look for
     * @return true if the accessor has a value, false if it doesn't
     */
    public boolean hasAccessor(Named accessor) {
        return mappings.containsKey(accessor.getName());
    }

    /**
     * Removes an accessor and any stored value from the internal map.
     * @param accessor The accessor to remove
     */
    public void removeAccessor(Named accessor) {
        mappings.remove(accessor.getName());
    }
}
