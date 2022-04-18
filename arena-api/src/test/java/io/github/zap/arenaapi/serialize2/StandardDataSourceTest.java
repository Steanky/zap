package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class StandardDataSourceTest {
    private static class MockDataContainer implements DataContainer {
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
    }

    private static class MockDataLoader implements DataLoader<MockDataContainer> {
        private MockDataContainer lastWritten;

        @Override
        public @NotNull Optional<MockDataContainer> read() {
            return Optional.of(new MockDataContainer());
        }

        @Override
        public void write(@NotNull MockDataContainer container) {
            lastWritten = container;
        }

        @Override
        public @NotNull Optional<MockDataContainer> makeContainer(@NotNull Object object) {
            return Optional.of(new MockDataContainer());
        }
    }

    private final StandardDataSource standardDataSource = new StandardDataSource();

    @Test
    void testRegistration() {
        DataLoader<MockDataContainer> dataLoader = new MockDataLoader();
        standardDataSource.registerLoader(dataLoader, "key");

        DataLoader<? extends DataContainer> registeredLoader = standardDataSource.getLoader("key");
        Assertions.assertNotNull(registeredLoader);

        Optional<? extends DataContainer> containerOptional = standardDataSource.getLoader("key").read();

        Assertions.assertTrue(containerOptional.isPresent());
        Assertions.assertSame(dataLoader, registeredLoader);
    }

    @Test
    void testWrite() {
        MockDataLoader loader = new MockDataLoader();
        MockDataContainer container = new MockDataContainer();

        standardDataSource.registerLoader(loader, "key");
        standardDataSource.writeObject(container, "key");

        Assertions.assertNotNull(loader.lastWritten);
    }
}