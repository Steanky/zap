package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

public interface HotbarProfile extends Iterable<HotbarObject> {
    @NotNull PlayerView getOwner();

    void putObject(@NotNull HotbarObject object);

    void deleteObjectInSlot(int slot);

    void swapObjects(int indexFrom, int indexTo);

    HotbarObject[] getObjects();

    HotbarObject getObject(int slot);

    void setActive(boolean active);

    boolean isActive();

    @NotNull HotbarGroupView asGroupView();

    static @NotNull HotbarProfile newProfile(@NotNull PlayerView owner) {
        return new BasicHotbarProfile(owner);
    }
}
