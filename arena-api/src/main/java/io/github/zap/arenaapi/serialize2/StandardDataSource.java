package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StandardDataSource implements DataSource {
    private final Map<DataKey, DataLoader> loaders = new HashMap<>();

    @Override
    public @NotNull Optional<DataContainer> pullContainer(@NotNull DataKey key) {
        DataLoader loader = loaders.get(key);

        if(loader != null) {
            return loader.read();
        }

        return Optional.empty();
    }

    @Override
    public void pushContainer(@NotNull DataContainer container, @NotNull DataKey key) {
        DataLoader loader = loaders.get(key);

        if(loader != null) {
            loader.write(container);
        }
    }

    @Override
    public void associateLoader(@NotNull DataLoader loader, @NotNull DataKey key) {
        loaders.put(key, loader);
    }
}
