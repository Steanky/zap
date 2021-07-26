package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("ClassCanBeRecord") //no it can't, map field should not be public smh my head
class StandardDataContainer implements DataContainer {
    private final Map<String, Object> map;

    StandardDataContainer(@NotNull Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public @NotNull <T> Optional<T> getObject(@NotNull DataKey key) {
        //noinspection unchecked
        return Optional.ofNullable((T)map.get(key.key()));
    }

    @Override
    public <T> T getObjectOrDefault(@NotNull DataKey key, T fallback) {
        //noinspection unchecked
        T stored = (T)map.get(key.key());
        return stored == null ? fallback : stored;
    }
}
