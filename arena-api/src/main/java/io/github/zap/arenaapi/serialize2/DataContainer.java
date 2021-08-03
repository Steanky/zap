package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

/**
 * An object containing a number of key-value pairs.
 */
public interface DataContainer {
    @NotNull <T> Optional<T> getObject(@NotNull Class<T> type, @NotNull DataKey ... keys);

    @NotNull <T> Optional<T> getObject(@NotNull TypeInformation typeInformation, @NotNull DataKey ... keys);

    @NotNull Optional<String> getString(@NotNull DataKey... keys);

    @NotNull Optional<Boolean> getBoolean(@NotNull DataKey... keys);

    @NotNull Optional<Byte> getByte(@NotNull DataKey... keys);

    @NotNull Optional<Short> getShort(@NotNull DataKey... keys);

    @NotNull Optional<Integer> getInt(@NotNull DataKey... keys);

    @NotNull Optional<Character> getChar(@NotNull DataKey... keys);

    @NotNull Optional<Long> getLong(@NotNull DataKey... keys);

    @NotNull Optional<Float> getFloat(@NotNull DataKey... keys);

    @NotNull Optional<Double> getDouble(@NotNull DataKey... keys);

    /**
     * Returns the data contained in this DataContainer, in the form of a "raw" map, with no conversions performed.
     * If one is intended to serialize this object, it is generally required to use a DataMarshal on this DataContainer
     * instead. This map should be a copy of the internal map, to ensure immutability of DataContainers.
     */
    @NotNull Map<String, Object> objectMapping();
}
