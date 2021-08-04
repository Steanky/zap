package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class StandardDataSource implements DataSource {
    private final Map<String, DataLoader> loaders = new HashMap<>();

    @Override
    public @NotNull Optional<DataContainer> readContainer(@NotNull String key) {
        DataLoader loader = loaders.get(key);

        if(loader != null) {
            return loader.read();
        }

        return Optional.empty();
    }

    @Override
    public void writeContainer(@NotNull Object data, @NotNull String key) {
        DataLoader loader = loaders.get(key);

        if(loader != null) {
            loader.factory().makeFrom(data).ifPresent(loader::write);
        }
    }

    @Override
    public void registerLoader(@NotNull DataLoader loader, @NotNull String key) {
        loaders.put(key, loader);
    }

    @Override
    public DataLoader getLoader(@NotNull String key) {
        return loaders.get(key);
    }
}
