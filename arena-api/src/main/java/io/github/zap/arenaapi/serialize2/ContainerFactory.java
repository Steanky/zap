package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A class that can produce DataContainer objects from data.
 */
public interface ContainerFactory {
    /**
     * Produces a DataContainer from an Object.
     *
     * If the DataContainer could not be constructed due to improper input, IO, or other reasons, this method will
     * return null and may log an error.
     * @param data The data to use
     * @return A DataContainer object containing a readable representation of the data, or null if it cannot be
     * constructed
     */
    @NotNull Optional<DataContainer> makeFrom(@NotNull Object data);
}
