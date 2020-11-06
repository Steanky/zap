package io.github.zap.game;

import lombok.Getter;

import java.util.*;

/**
 * Singleton responsible for handling accessors and ensuring that their internal maps are cleared to prevent
 * "memory leaks".
 */
public class AccessorManager {
    @Getter
    private static final AccessorManager instance = new AccessorManager();

    private final Map<String, Set<MultiAccessor<?>>> mappings = new HashMap<>();

    private AccessorManager() {}

    /**
     * Clears all the mappings associated with the specific accessor.
     * @param accessor The accessor object
     */
    public void removeMappingsFor(Named accessor) {
        String accessorName = accessor.getName();
        Set<MultiAccessor<?>> values = mappings.get(accessorName);

        if(values != null) {
            for(MultiAccessor<?> multiAccessor : values) {
                multiAccessor.removeAccessor(accessor);
            }

            mappings.remove(accessorName);
        }
    }

    /**
     * Adds an accessor
     * @param accessor The object accessing the specified MultiAccessor
     * @param variable The MultiAccessor it referenced
     */
    public void addAccessor(Named accessor, MultiAccessor<?> variable) {
        Set<MultiAccessor<?>> set = mappings.get(accessor.getName());
        if(set == null) {
            set = new HashSet<>();
        }

        set.add(variable); //disallow duplicate elements
    }

    /**
     * Removes an accessor mapping.
     * @param accessor The accessing object
     * @param value The value to remove
     */
    public void removeAccessor(Named accessor, MultiAccessor<?> value) {
        Set<MultiAccessor<?>> set = mappings.get(accessor.getName());
        if(set != null) {
            set.remove(value);
        }
    }
}
