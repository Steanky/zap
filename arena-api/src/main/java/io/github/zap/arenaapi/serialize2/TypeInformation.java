package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

public record TypeInformation(@NotNull Class<?> type, @NotNull Class<?> ... typeParameters) { }
