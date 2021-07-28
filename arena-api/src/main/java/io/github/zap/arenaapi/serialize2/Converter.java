package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

public interface Converter<From> {
    @NotNull Class<From> convertsFrom();

    Object convert(@NotNull From from, @NotNull Class<?> toClass);

    boolean canConvertTo(@NotNull Class<?> type);
}
