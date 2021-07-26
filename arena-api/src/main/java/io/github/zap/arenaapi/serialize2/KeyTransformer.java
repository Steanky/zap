package io.github.zap.arenaapi.serialize2;


import org.jetbrains.annotations.NotNull;

/**
 * Used to perform operations on valid keys during data loading. May be used to implement aliases, refactor data, or
 * other tasks.
 */
@FunctionalInterface
public interface KeyTransformer {
    KeyTransformer DO_NOTHING = key -> key;

    @NotNull String transform(@NotNull String key);
}
