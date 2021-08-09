package io.github.zap.arenaapi.hotbar2;

import io.github.zap.arenaapi.serialize2.DataContainer;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface HotbarObjectFactory {
    /**
     * Creates a new HotbarObject given some input data, a {@link PlayerView}, and a slot.
     * @param data The data this HotbarObjectFactory uses to construct HotbarObjects
     * @param playerView The owner of the HotbarObject
     * @param slot The slot to create the HotbarObject in
     * @return A new HotbarObject linked to the provided profile
     * @throws IllegalArgumentException When the provided data does not contain the fields needed to construct the object
     */
    @NotNull HotbarObject make(@NotNull DataContainer data, @NotNull PlayerView playerView, int slot)
            throws IllegalArgumentException;
}
