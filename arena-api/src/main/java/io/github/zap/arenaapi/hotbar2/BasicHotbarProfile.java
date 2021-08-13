package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

class BasicHotbarProfile implements HotbarProfile {
    private final HotbarObject[] objects = new HotbarObject[9];
    private final HotbarGroupView groupView = new BasicHotbarGroupView(this);

    BasicHotbarProfile() {}

    @Override
    public void putObject(@NotNull HotbarObject object, int slot) {
        HotbarObject oldObject = objects[slot];

        if(oldObject != null) {
            oldObject.cleanup();
        }

        objects[slot] = object;
    }

    @Override
    public void deleteObjectInSlot(int slot) {
        HotbarObject delete = objects[slot];

        if(delete != null) {
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
            fromCopy = from.copy();
            from.cleanup();
        }

        if(to != null) {
            toCopy = to.copy();
            to.cleanup();
        }

        objects[indexTo] = fromCopy;
        objects[indexFrom] = toCopy;
    }

    @Override
    public HotbarObject.Slotted[] getObjects() {
        HotbarObject.Slotted[] newArray = new HotbarObject.Slotted[objects.length];

        for(int i = 0; i < objects.length; i++) {
            newArray[i] = new HotbarObject.Slotted(objects[i], i);
        }

        return newArray;
    }

    @Override
    public int indexOf(@NotNull HotbarObject object) {
        for(int i = 0; i < objects.length; i++) {
            if(object == objects[i]) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public @NotNull HotbarObject.Slotted getObject(int slot) {
        return new HotbarObject.Slotted(objects[slot], slot);
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
