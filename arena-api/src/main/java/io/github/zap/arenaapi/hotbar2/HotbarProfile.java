package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

public interface HotbarProfile {
    @NotNull PlayerView getOwner();

    void putObjectInSlot(@NotNull HotbarObject object, int slot);

    void deleteObjectInSlot(int slot);

    HotbarObject getObject(int slot);

    static @NotNull HotbarProfile newProfile(@NotNull PlayerView owner) {
        return new BasicHotbarProfile(owner);
    }
}
