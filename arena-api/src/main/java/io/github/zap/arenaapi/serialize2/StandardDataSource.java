package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.*;

class StandardDataSource implements DataSource {
    private final Map<String, DataLoader<? extends DataContainer>> loaders = new HashMap<>();

    @Override
    public @NotNull Optional<? extends DataContainer> readContainer(@NotNull String key) {
        DataLoader<? extends DataContainer> loader = loaders.get(key);

        if(loader != null) {
            return loader.read();
        }

        return Optional.empty();
    }

    @Override
    public void writeObject(@NotNull Object data, @NotNull String key) {
        DataLoader<? extends DataContainer> loader = loaders.get(key);

        if(loader != null) {
            writeHelper(loader, data);
        }
    }

    @Override
    public void registerLoader(@NotNull DataLoader<? extends DataContainer> loader, @NotNull String key) {
        loaders.put(key, loader);
    }

    @Override
    public DataLoader<? extends DataContainer> getLoader(@NotNull String key) {
        return loaders.get(key);
    }

    //oh boy do i love java generics
    private <T extends DataContainer> void writeHelper(DataLoader<T> loader, Object data) {
        loader.makeContainer(data).ifPresent(loader::write);
    }
}
