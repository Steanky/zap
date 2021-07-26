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
    public @NotNull <T> Optional<T> getObject(@NotNull DataKey key, @NotNull Class<T> type) {
        Object data = map.get(key.key());

        if(data == null) {
            return Optional.empty();
        }
        else if(type.isAssignableFrom(data.getClass())) {
            //noinspection unchecked
            return Optional.of((T)data);
        }

        return Optional.empty();
    }

    @Override
    public @NotNull Optional<DataContainer> getChild(@NotNull DataKey key) {
        return getObject(key, DataContainer.class);
    }

    @Override
    public @NotNull Optional<String> getString(@NotNull DataKey key) {
        return getObject(key, String.class);
    }

    @Override
    public @NotNull Optional<Boolean> getBoolean(@NotNull DataKey key) {
        return getObject(key, Boolean.class);
    }

    @Override
    public @NotNull Optional<Byte> getByte(@NotNull DataKey key) {
        return getObject(key, Byte.class);
    }

    @Override
    public @NotNull Optional<Short> getShort(@NotNull DataKey key) {
        return getObject(key, Short.class);
    }

    @Override
    public @NotNull Optional<Integer> getInt(@NotNull DataKey key) {
        return getObject(key, Integer.class);
    }

    @Override
    public @NotNull Optional<Character> getChar(@NotNull DataKey key) {
        return getObject(key, Character.class);
    }

    @Override
    public @NotNull Optional<Long> getLong(@NotNull DataKey key) {
        return getObject(key, Long.class);
    }

    @Override
    public @NotNull Optional<Float> getFloat(@NotNull DataKey key) {
        return getObject(key, Float.class);
    }

    @Override
    public @NotNull Optional<Double> getDouble(@NotNull DataKey key) {
        return getObject(key, Double.class);
    }

    @Override
    public @NotNull Map<String, Object> objectMapping() {
        return Map.copyOf(map);
    }
}