package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class BasicHotbarGroupView implements HotbarGroupView {
    private final HotbarProfile profile;
    private final Map<String, int[]> groupMap = new HashMap<>();

    BasicHotbarGroupView(@NotNull HotbarProfile profile) {
        this.profile = profile;
    }

    private boolean validateNewSlots(int[] newSlots) {
        for(int newSlot : newSlots) {
            if(newSlot < 0 || newSlot > 8) {
                return false;
            }

            for(int[] group : groupMap.values()) {
                for(int groupSlot : group) {
                    if(groupSlot == newSlot) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private int[] getGroupInternal(String groupName) {
        int[] group = groupMap.get(groupName);
        if(group != null) {
            return group;
        }

        throw new IllegalArgumentException("Group named " + groupName + " is not registered");
    }

    @Override
    public @NotNull HotbarProfile getProfile() {
        return profile;
    }

    @Override
    public void registerGrouping(@NotNull String groupName, int... slots) {
        if(!groupMap.containsKey(groupName) && validateNewSlots(slots)) {
            groupMap.put(groupName, Arrays.copyOf(slots, slots.length));
            return;
        }

        throw new IllegalArgumentException("Illegal arguments: " + groupName + ", slots " + Arrays.toString(slots));
    }

    @Override
    public void appendObjectToGroup(@NotNull String groupName, @NotNull HotbarObject object) {
        for(int slot : getGroupInternal(groupName)) {
            if(profile.getObject(slot) == null) {
                profile.putObject(object, slot);
                return;
            }
        }

        throw new IllegalArgumentException("Hotbar group " + groupName + " is full");
    }

    @Override
    public HotbarObject[] getObjectsFromGroup(@NotNull String groupName) {
        int[] slots = getGroupInternal(groupName);
        HotbarObject[] objects = new HotbarObject[slots.length];

        for(int i = 0; i < slots.length; i++) {
            objects[i] = profile.getObject(slots[i]);
        }

        return objects;
    }

    @Override
    public int getCapacityOfGroup(@NotNull String groupName) {
        return getGroupInternal(groupName).length;
    }

    @Override
    public int getSizeOfGroup(@NotNull String groupName) {
        int size = 0;
        for(int slot : getGroupInternal(groupName)) {
            HotbarObject object = profile.getObject(slot);

            if(object != null) {
                size++;
            }
        }

        return size;
    }

    @Override
    public void removeObjectFromGroup(@NotNull String groupName, int groupIndex, boolean collapse) {
        int[] group = getGroupInternal(groupName);

        int previousHotbarIndex = group[groupIndex];
        profile.deleteObjectInSlot(previousHotbarIndex);

        if(collapse) {
            for(int i = groupIndex + 1; i < group.length; i++) {
                int currentHotbarIndex = group[i];
                profile.swapObjects(currentHotbarIndex, previousHotbarIndex);
                previousHotbarIndex = currentHotbarIndex;
            }
        }
    }
}
