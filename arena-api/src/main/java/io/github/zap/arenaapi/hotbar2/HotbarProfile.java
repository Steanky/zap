package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

public interface HotbarProfile extends Iterable<HotbarObject> {
    void putObject(@NotNull HotbarObject object);

    void deleteObjectInSlot(int slot);

    void swapObjects(int indexFrom, int indexTo);

    HotbarObject[] getObjects();

    HotbarObject getObject(int slot);

    void setActive(boolean active);

    boolean isActive();

    void refreshAll();

    @NotNull HotbarGroupView asGroupView();

    static @NotNull HotbarProfile newProfile() {
        return new BasicHotbarProfile();
    }
}
