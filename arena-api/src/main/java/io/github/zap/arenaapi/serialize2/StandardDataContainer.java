package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("ClassCanBeRecord") //no it can't, map field should not be public smh my head
class StandardDataContainer implements DataContainer {
    private final Map<String, Object> map;

    StandardDataContainer(@NotNull Map<String, Object> map) {
        this.map = map;
    }

    private @NotNull <T> Optional<T> getObjectInternal(StandardDataContainer container, DataKey key, Class<T> type) {
        Object data = container.map.get(key.key());

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
    public @NotNull <T> Optional<T> getObject(@NotNull Class<T> type, @NotNull DataKey... keys) {
        if(keys.length == 0) {
            throw new IllegalArgumentException("getObject called without providing any DataKeys");
        }

        StandardDataContainer lastContainer = this;
        for(int i = 0; i < keys.length - 1; i++) {
            DataKey key = keys[i];
            Optional<StandardDataContainer> object = getObjectInternal(lastContainer, key, StandardDataContainer.class);

            if(object.isEmpty()) {
                return Optional.empty();
            }
            else {
                lastContainer = object.get();
            }
        }

        return getObjectInternal(lastContainer, keys[keys.length - 1], type);
    }

    @Override
    public @NotNull Optional<String> getString(@NotNull DataKey key) {
        return getObjectInternal(this, key, String.class);
    }

    @Override
    public @NotNull Optional<Boolean> getBoolean(@NotNull DataKey key) {
        return getObjectInternal(this, key, Boolean.class);
    }

    @Override
    public @NotNull Optional<Byte> getByte(@NotNull DataKey key) {
        return getObjectInternal(this, key, Byte.class);
    }

    @Override
    public @NotNull Optional<Short> getShort(@NotNull DataKey key) {
        return getObjectInternal(this, key, Short.class);
    }

    @Override
    public @NotNull Optional<Integer> getInt(@NotNull DataKey key) {
        return getObjectInternal(this, key, Integer.class);
    }

    @Override
    public @NotNull Optional<Character> getChar(@NotNull DataKey key) {
        return getObjectInternal(this, key, Character.class);
    }

    @Override
    public @NotNull Optional<Long> getLong(@NotNull DataKey key) {
        return getObjectInternal(this, key, Long.class);
    }

    @Override
    public @NotNull Optional<Float> getFloat(@NotNull DataKey key) {
        return getObjectInternal(this, key, Float.class);
    }

    @Override
    public @NotNull Optional<Double> getDouble(@NotNull DataKey key) {
        return getObjectInternal(this, key, Double.class);
    }

    @Override
    public @NotNull Map<String, Object> objectMapping() {
        return new LinkedHashMap<>(map);
    }
}