package io.github.zap.arenaapi.hotbar2;

import io.github.zap.arenaapi.serialize2.DataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DataHotbarObjectFactory implements HotbarObjectFactory {
    private static final DataHotbarObjectFactory INSTANCE = new DataHotbarObjectFactory();

    private static final String TYPE_FIELD = "type";

    private final Map<String, HotbarObjectFactory> factories = new HashMap<>();

    private DataHotbarObjectFactory() {}

    public static @NotNull DataHotbarObjectFactory instance() {
        return INSTANCE;
    }

    @Override
    public @NotNull HotbarObject make(@NotNull DataContainer container, @NotNull PlayerView playerView, int slot)
            throws IllegalArgumentException {
        Optional<String> optionalType = container.getString(TYPE_FIELD);

        if(optionalType.isPresent()) {
            String type = optionalType.get();
            HotbarObjectFactory factory = factories.get(type);

            if(factory != null) {
                return factory.make(container, playerView, slot);
            }

            throw new IllegalArgumentException("No registered factory present for type field " + type);
        }

        throw new IllegalArgumentException("Type field not present for DataContainer " + container);
    }

    public void registerFactory(@NotNull String type, @NotNull HotbarObjectFactory factory) {
        if(!(factory instanceof DataHotbarObjectFactory)) { //naive attempt to prevent bad recursive registration
            factories.put(type, factory);
        }
    }
}
