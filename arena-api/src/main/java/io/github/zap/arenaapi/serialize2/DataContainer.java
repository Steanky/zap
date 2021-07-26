package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * An object containing a set of key-value pairs.
 */
public interface DataContainer {
    <T> @NotNull Optional<T> getObject(@NotNull DataKey key);

    <T> T getObjectOrDefault(@NotNull DataKey key, T fallback);
}
