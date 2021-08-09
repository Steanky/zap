package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

/**
 * An object containing a number of key-value pairs.
 */
public interface DataContainer {
    @NotNull <T> Optional<T> getObject(@NotNull Class<T> type, @NotNull String... keys);

    @NotNull <T> Optional<T> getObject(@NotNull TypeToken<T> typeToken, @NotNull String ... keys);

    @NotNull Optional<String> getString(@NotNull String... keys);

    @NotNull Optional<Boolean> getBoolean(@NotNull String... keys);

    @NotNull Optional<Byte> getByte(@NotNull String... keys);

    @NotNull Optional<Short> getShort(@NotNull String... keys);

    @NotNull Optional<Integer> getInt(@NotNull String... keys);

    @NotNull Optional<Character> getChar(@NotNull String... keys);

    @NotNull Optional<Long> getLong(@NotNull String... keys);

    @NotNull Optional<Float> getFloat(@NotNull String... keys);

    @NotNull Optional<Double> getDouble(@NotNull String... keys);

    DataContainer EMPTY = new DataContainer() {
        @Override
        public @NotNull <T> Optional<T> getObject(@NotNull Class<T> type, @NotNull String... keys) {
            return Optional.empty();
        }

        @Override
        public @NotNull <T> Optional<T> getObject(@NotNull TypeToken<T> typeToken, @NotNull String... keys) {
            return Optional.empty();
        }

        @Override
        public @NotNull Optional<String> getString(@NotNull String... keys) {
            return Optional.empty();
        }

        @Override
        public @NotNull Optional<Boolean> getBoolean(@NotNull String... keys) {
            return Optional.empty();
        }

        @Override
        public @NotNull Optional<Byte> getByte(@NotNull String... keys) {
            return Optional.empty();
        }

        @Override
        public @NotNull Optional<Short> getShort(@NotNull String... keys) {
            return Optional.empty();
        }

        @Override
        public @NotNull Optional<Integer> getInt(@NotNull String... keys) {
            return Optional.empty();
        }

        @Override
        public @NotNull Optional<Character> getChar(@NotNull String... keys) {
            return Optional.empty();
        }

        @Override
        public @NotNull Optional<Long> getLong(@NotNull String... keys) {
            return Optional.empty();
        }

        @Override
        public @NotNull Optional<Float> getFloat(@NotNull String... keys) {
            return Optional.empty();
        }

        @Override
        public @NotNull Optional<Double> getDouble(@NotNull String... keys) {
            return Optional.empty();
        }
    };
}
