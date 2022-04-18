package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

class BasicHotbarProfile implements HotbarProfile {
    private final HotbarObject[] objects = new HotbarObject[9];
    private final HotbarGroupView groupView = new BasicHotbarGroupView(this);

    private HotbarObject lastObject = null;
    private int lastIndex = -1;

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

        objects[indexTo] = from;
        objects[indexFrom] = to;
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
        if(object == lastObject) { //for render loops that frequently index the same item, don't iterate again
            return lastIndex;
        }

        for(int i = 0; i < objects.length; i++) {
            HotbarObject sample = objects[i];
            if(object == sample) {
                lastObject = sample;
                return lastIndex = i;
            }
        }

        return lastIndex = -1;
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
    public @NotNull Iterator<HotbarObject.Slotted> iterator() {
        //this is a somewhat questionable hack but it absolutely works
        AtomicInteger index = new AtomicInteger(-1);
        return Arrays.stream(objects).filter((object) ->
        {
            index.incrementAndGet();
            return object != null;
        }).map((object) -> new HotbarObject.Slotted(object, index.get())).iterator();
    }
}
