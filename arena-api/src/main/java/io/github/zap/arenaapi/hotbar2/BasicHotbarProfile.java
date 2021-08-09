package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

class BasicHotbarProfile implements HotbarProfile {
    private final PlayerView owner;
    private final HotbarObject[] objects = new HotbarObject[9];

    BasicHotbarProfile(@NotNull PlayerView owner) {
        this.owner = owner;
    }

    @Override
    public @NotNull PlayerView getOwner() {
        return owner;
    }

    @Override
    public void putObjectInSlot(@NotNull HotbarObject object, int slot) {
        HotbarObject existingObject = objects[slot];
        if(existingObject != null) {
            existingObject.onDeactivate();
        }

        object.onActivate();
        objects[slot] = object;
    }

    @Override
    public void deleteObjectInSlot(int slot) {
        HotbarObject delete = objects[slot];

        if(delete != null) {
            delete.onDeactivate();
            objects[slot] = null;
        }
    }

    @Override
    public HotbarObject getObject(int slot) {
        return objects[slot];
    }
}
