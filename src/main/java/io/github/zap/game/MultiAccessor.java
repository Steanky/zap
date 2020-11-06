package io.github.zap.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic utility class that enables different named accessor objects to access their own unique copies of a single
 * value. Specifically, this is used by some map data classes to store state information that is game-specific (such
 * as whether or not a door is open). The value will be different for each accessing ZombiesArena. Furthermore, the
 * mappings will be removed when the arena closes thanks to the AccessorManager.
 * @param <T> The type of value this accessor stores
 */
@RequiredArgsConstructor
public class MultiAccessor<T> {
    private final Map<String, T> mappings = new HashMap<>();

    @Getter
    private final T defaultValue;

    /**
     * Gets the value that is stored for the given Named. Returns the default value if it is not in the internal map.
     * @param named The Named object attempting to access this instance, which should have a unique name
     * @return The value stored for the provided Named object, or the default value used by this instance
     */
    public T get(Named named) {
        return mappings.getOrDefault(named.getName(), defaultValue);
    }

    /**
     * Sets the value associated with this Named instance.
     * @param named The enacting Named instance
     * @param value The value to store for Named
     * @return true if the stored value changed as a result of this call, false otherwise
     */
    public boolean set(Named named, T value) {
        String accessorName = named.getName();
        T oldValue = mappings.get(accessorName);

        if(!value.equals(oldValue)) {
            mappings.put(accessorName, value);
            AccessorManager.getInstance().addAccessor(named, this);
            return true;
        }

        return false;
    }

    /**
     * Removes the mapping for the provided Named object.
     * @param named The Named instance to remove the mapping for
     */
    public void removeAccessor(Named named) {
        mappings.remove(named.getName());
    }
}
