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
            fromCopy = from.copyInSlot(indexTo);
            from.cleanup();
        }

        if(to != null) {
            toCopy = to.copyInSlot(indexFrom);
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
    public int indexOf(@NotNull HotbarObject hotbarObject) {
        int i = 0;
        for(HotbarObject object : objects) {
            if(object == hotbarObject) {
                return i;
            }

            i++;
        }

        return -1;
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
