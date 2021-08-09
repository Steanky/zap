package io.github.zap.arenaapi.hotbar2;

import io.github.zap.arenaapi.serialize2.DataContainer;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface HotbarObjectFactory {
    /**
     * Creates a new HotbarObject given a {@link DataContainer} and {@link PlayerView}
     * @param container A DataContainer instance containing necessary fields
     * @param playerView The owner of the HotbarObject
     * @param slot The slot to create the HotbarObject in
     * @return A new HotbarObject linked to the provided profile
     * @throws IllegalArgumentException When the provided data does not contain the fields needed to construct the object
     */
    @NotNull HotbarObject make(@NotNull DataContainer container, @NotNull PlayerView playerView, int slot) throws IllegalArgumentException;
}
