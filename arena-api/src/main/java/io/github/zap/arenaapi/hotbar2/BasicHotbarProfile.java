package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

class BasicHotbarProfile implements HotbarProfile {
    private final PlayerView owner;
    private final HotbarObject[] objects = new HotbarObject[9];
    private final HotbarGroupView groupView = new BasicHotbarGroupView(this);

    boolean active = false;

    BasicHotbarProfile(@NotNull PlayerView owner) {
        this.owner = owner;
    }

    @Override
    public void setActive(boolean active) {
        if(this.active != active) {
            for(HotbarObject object : this) {
                if(active) {
                    object.show();
                }
                else {
                    object.hide();
                }
            }

            this.active = active;
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public @NotNull PlayerView getOwner() {
        return owner;
    }

    @Override
    public void putObject(@NotNull HotbarObject object) {
        int slot = object.getSlot();
        HotbarObject existingObject = objects[slot];

        if(active) {
            if(existingObject != null) {
                existingObject.hide();
            }

            object.show();
        }

        objects[slot] = object;
    }

    @Override
    public void deleteObjectInSlot(int slot) {
        HotbarObject delete = objects[slot];

        if(active) {
            if(delete != null) {
                delete.hide();
            }

            objects[slot] = null;
        }
    }

    @Override
    public void swapObjects(int indexFrom, int indexTo) {
        HotbarObject from = objects[indexFrom];
        HotbarObject to = objects[indexTo];

        HotbarObject fromCopy = null;
        HotbarObject toCopy = null;

        if(from != null) {
            fromCopy = from.copyInSlot(indexTo);

            from.hide();
            fromCopy.show();
        }

        if(to != null) {
            toCopy = to.copyInSlot(indexFrom);

            to.hide();
            toCopy.show();
        }

        objects[indexTo] = fromCopy;
        objects[indexFrom] = toCopy;
    }

    @Override
    public HotbarObject[] getObjects() {
        return Arrays.copyOf(objects, objects.length);
    }

    @Override
    public HotbarObject getObject(int slot) {
        return objects[slot];
    }

    @Override
    public @NotNull HotbarGroupView asGroupView() {
        return groupView;
    }

    @Override
    public @NotNull Iterator<HotbarObject> iterator() {
        return Arrays.stream(objects).filter(Objects::nonNull).iterator();
    }
}
