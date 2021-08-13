package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

public interface HotbarProfile extends Iterable<HotbarObject.Slotted> {
    void putObject(@NotNull HotbarObject object, int slot);

    void deleteObjectInSlot(int slot);

    void swapObjects(int indexFrom, int indexTo);

    HotbarObject.Slotted[] getObjects();

    int indexOf(@NotNull HotbarObject object);

    @NotNull HotbarObject.Slotted getObject(int slot);

    @NotNull HotbarGroupView asGroupView();

    static @NotNull HotbarProfile newProfile() {
        return new BasicHotbarProfile();
    }
}
