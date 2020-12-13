package io.github.zap.arenaapi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * Generic utility class that enables different named accessor objects to access their own unique copies of a single
 * value. Specifically, this is used by some map data classes to store state information that is game-specific (such
 * as whether or not a door is open). The value will be different for each accessing ZombiesArena. Furthermore, the
 * mappings can be easily removed with a single call to removeMappingsFor().
 * @param <T> The type of value this accessor stores
 */
@RequiredArgsConstructor
public class Property<T> {
    private static final Map<UUID, Set<Property<?>>> globalMappings = new HashMap<>();

    private final Map<UUID, T> mappings = new HashMap<>();

    @Getter
    private final T defaultValue;

    /**
     * Gets the value that is stored for the given Named. Returns the default value if it is not in the internal map.
     * @param unique The Named object attempting to access this instance, which should have a unique name
     * @return The value stored for the provided Named object, or the default value used by this instance
     */
    public T get(Unique unique) {
        return mappings.getOrDefault(unique.getId(), defaultValue);
    }

    /**
     * Sets the value associated with this Named instance.
     * @param unique The enacting Named instance
     * @param value The value to store for Named
     */
    public void set(Unique unique, T value) {
        UUID id = unique.getId();
        mappings.put(id, value);

        Set<Property<?>> set = globalMappings.get(id);
        if(set == null) {
            set = new HashSet<>();
        }

        set.add(this);
    }

    /**
     * Removes the mapping for the provided Named object.
     * @param unique The Named instance to remove the mapping for
     */
    public void removeAccessor(Unique unique) {
        mappings.remove(unique.getId());
    }

    /**
     * Clears all the mappings associated with the specific accessor.
     * @param accessor The accessor object
     */
    public static void removeMappingsFor(Unique accessor) {
        UUID id = accessor.getId();
        Set<Property<?>> values = globalMappings.get(id);

        if(values != null) {
            for(Property<?> property : values) {
                property.removeAccessor(accessor);
            }

            globalMappings.remove(id);
        }
    }
}
