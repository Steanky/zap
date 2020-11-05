package io.github.zap.game;

import lombok.Getter;

import java.util.*;

public class AccessorManager {
    @Getter
    private static final AccessorManager instance = new AccessorManager();

    private final Map<String, Set<MultiAccessor<?>>> mappings = new HashMap<>();

    private AccessorManager() {}

    /**
     * Clears all the mappings associated with the specific accessor.
     * @param accessorName The name of the accessor
     */
    public void removeMappingsFor(String accessorName) {
        Set<MultiAccessor<?>> values = mappings.get(accessorName);

        if(values != null) {
            for(MultiAccessor<?> accessor : values) {
                accessor.removeAccessor(accessorName);
            }

            mappings.remove(accessorName);
        }
    }

    /**
     * Adds an accessor mapping.
     * @param accessorName The name of the accessor
     * @param variable The MultiAccessor it referenced
     */
    public void addAccessor(String accessorName, MultiAccessor<?> variable) {
        Set<MultiAccessor<?>> list = mappings.getOrDefault(accessorName, null);
        if(list == null) {
            list = new HashSet<>();
        }

        list.add(variable);
    }

    /**
     * Removes an accessor mapping.
     * @param accessorName The name of the accessor to remove
     * @param value The value to remove
     */
    public void removeAccessor(String accessorName, MultiAccessor<?> value) {
        Set<MultiAccessor<?>> list = mappings.getOrDefault(accessorName, null);
        if(list != null) {
            list.remove(value);
        }
    }
}
