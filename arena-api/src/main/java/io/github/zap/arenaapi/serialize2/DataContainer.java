package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

/**
 * An object containing a number of key-value pairs.
 */
public interface DataContainer {
    @NotNull <T> Optional<T> getObject(@NotNull DataKey key, @NotNull Class<T> type);

    @NotNull Optional<DataContainer> getChild(@NotNull DataKey key);

    @NotNull Optional<String> getString(@NotNull DataKey key);

    @NotNull Optional<Boolean> getBoolean(@NotNull DataKey key);

    @NotNull Optional<Byte> getByte(@NotNull DataKey key);

    @NotNull Optional<Short> getShort(@NotNull DataKey key);

    @NotNull Optional<Integer> getInt(@NotNull DataKey key);

    @NotNull Optional<Character> getChar(@NotNull DataKey key);

    @NotNull Optional<Long> getLong(@NotNull DataKey key);

    @NotNull Optional<Float> getFloat(@NotNull DataKey key);

    @NotNull Optional<Double> getDouble(@NotNull DataKey key);

    @NotNull Map<String, Object> objectMapping();
}
