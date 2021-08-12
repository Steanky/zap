package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

class BasicHotbarProfile implements HotbarProfile {
    private final HotbarObject[] objects = new HotbarObject[9];
    private final HotbarGroupView groupView = new BasicHotbarGroupView(this);

    boolean active = false;

    BasicHotbarProfile() {}

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
    public void refreshAll() {
        for(HotbarObject object : this) {
            object.refresh();
        }
    }

    @Override
    public void putObject(@NotNull HotbarObject object) {
        int slot = object.getSlot();
        HotbarObject existingObject = objects[slot];

        if(existingObject != null) {
            if(active) {
                existingObject.hide();
            }

            existingObject.cleanup();
        }

        object.show();
        objects[slot] = object;
    }

    @Override
    public void deleteObjectInSlot(int slot) {
        HotbarObject delete = objects[slot];

        if(delete != null) {
            if(active) {
                delete.hide();
            }

            delete.cleanup();
        }

        objects[slot] = null;
    }

    @Override
    public void swapObjects(int indexFrom, int indexTo) {
        HotbarObject from = objects[indexFrom];
        HotbarObject to = objects[indexTo];

        HotbarObject fromCopy = null;
        HotbarObject toCopy = null;

        if(from != null) {
            fromCopy = from.copyInSlot(indexTo);

            if(active) {
                from.hide();
                fromCopy.show();
            }

            from.cleanup();
        }

        if(to != null) {
            toCopy = to.copyInSlot(indexFrom);

            if(active) {
                to.hide();
                toCopy.show();
            }

            to.cleanup();
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
