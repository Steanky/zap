package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

public interface Converter<From> {
    @NotNull Class<From> convertsFrom();

    Object convert(@NotNull From from, @NotNull TypeInformation typeInformation);

    boolean canConvertTo(@NotNull Class<?> type);
}
