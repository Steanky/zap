package io.github.zap.arenaapi.hotbar2;

import io.github.zap.arenaapi.serialize2.DataContainer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

class BasicHotbarProfile implements HotbarProfile {
    private final PlayerView owner;
    private final HotbarObjectFactory objectFactory;
    private final HotbarObject[] objects = new HotbarObject[9];

    BasicHotbarProfile(@NotNull PlayerView owner, @NotNull HotbarObjectFactory objectFactory) {
        this.owner = owner;
        this.objectFactory = objectFactory;
    }

    @Override
    public @NotNull PlayerView owner() {
        return owner;
    }

    @Override
    public @Nullable HotbarObject newObjectForSlot(@NotNull DataContainer objectData, int slot)
            throws IllegalArgumentException {
        HotbarObject object = objectFactory.make(objectData, owner, slot);
        int objectSlot = object.slot();

        HotbarObject existingObject = objects[objectSlot];
        if(existingObject != null) { //call deactivate on replace
            existingObject.onDeactivate();
        }

        object.onActivate();
        objects[objectSlot] = object;

        return object;
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
