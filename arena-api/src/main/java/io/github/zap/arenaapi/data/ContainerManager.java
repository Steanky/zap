package io.github.zap.arenaapi.data;

import io.github.zap.arenaapi.ArenaApi;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manager class that holds container instances for a certain
 */
public class ContainerManager {
    private final Map<String, Container<?,?>> containers = new HashMap<>();

    public Container<?, ?> getContainer(String name) {
        return containers.get(name);
    }

    public void addContainer(Container<?, ?> container) {
        if(containers.putIfAbsent(container.name(), container) != null) {
            ArenaApi.warning(String.format("Tried to register a container named %s, but one already exists",
                    container.name()));
        }
    }
}
