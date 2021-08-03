package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

public record TypeInformation<T>(@NotNull Class<? super T> type, @NotNull TypeInformation<?> ... parameters) { }
