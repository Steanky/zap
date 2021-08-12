package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

class BasicHotbarProfile implements HotbarProfile {
    private final HotbarCanvas canvas;
    private final HotbarObject[] objects = new HotbarObject[9];
    private final HotbarGroupView groupView = new BasicHotbarGroupView(this);

    BasicHotbarProfile(@NotNull HotbarCanvas canvas) {
        this.canvas = canvas;
    }

    private void redrawSlotInternal(int slot) {
        HotbarObject object = objects[slot];

        if(object == null) {
            canvas.drawItem(null, slot);
        }
        else {
            canvas.drawItem(object.getStack(), slot);
        }
    }

    @Override
    public void redrawObject(int slot) {
        redrawSlotInternal(slot);
    }

    @Override
    public void redrawAll() {
        for(int i = 0; i < objects.length; i++) {
            redrawSlotInternal(i);
        }
    }

    @Override
    public @NotNull HotbarCanvas getCanvas() {
        return canvas;
    }

    @Override
    public void putObject(@NotNull HotbarObject object) {
        int slot = object.getSlot();
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
    public @NotNull HotbarGroupView asGroupView() {
        return groupView;
    }

    @Override
    public @NotNull Iterator<HotbarObject> iterator() {
        return Arrays.stream(objects).filter(Objects::nonNull).iterator();
    }
}
