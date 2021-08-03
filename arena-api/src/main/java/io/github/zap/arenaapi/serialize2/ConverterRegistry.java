package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

public interface ConverterRegistry {
    ConverterRegistry EMPTY_REGISTRY = new ConverterRegistry() {
        @Override
        public <From> Converter<? super From> deserializerFor(@NotNull Class<? super From> from, @NotNull Class<?> to) {
            return null;
        }

        @Override
        public void registerDeserializer(@NotNull Converter<?> converter) { }
    };

    <From> Converter<? super From> deserializerFor(@NotNull Class<? super From> from, @NotNull Class<?> to);

    void registerDeserializer(@NotNull Converter<?> converter);
}
