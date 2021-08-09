package io.github.zap.arenaapi.hotbar2;

import io.github.zap.arenaapi.serialize2.DataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HotbarProfile {
    @NotNull PlayerView owner();

    /**
     * Creates a new {@link HotbarObject} from the given data and adds it to the profile, in the given slot.
     * @param objectData The DataContainer from which to create the data
     * @param slot The slot to create the data in
     * @return A new HotbarObject, if one was added to the player's inventory; or null if the player was invalid
     * @throws IllegalArgumentException If the given DataContainer is not valid for creating HotbarObject instances
     */
    @Nullable HotbarObject newObjectForSlot(@NotNull DataContainer objectData, int slot) throws IllegalArgumentException;

    void deleteObjectInSlot(int slot);

    HotbarObject getObject(int slot);

    static HotbarProfile newStandard(@NotNull PlayerView playerView, @NotNull HotbarObjectFactory factory) {
        return new BasicHotbarProfile(playerView, factory);
    }
}
