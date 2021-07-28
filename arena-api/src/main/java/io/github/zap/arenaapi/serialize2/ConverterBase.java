package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

public abstract class ConverterBase<From> implements Converter<From> {
    private final Class<From> from;

    ConverterBase(Class<From> from) {
        this.from = from;
    }

    @Override
    public @NotNull Class<From> convertsFrom() {
        return from;
    }
}
