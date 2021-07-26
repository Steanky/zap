package io.github.zap.arenaapi.hotbar;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The profile of a player that can be swapped between when the player is in different states
 */
public class HotbarProfile {

    public final static String DEFAULT_HOTBAR_OBJECT_GROUP_KEY = "Default";

    private final Map<String, HotbarObjectGroup> hotbarObjectGroupMap = new HashMap<>();

    private final Player player;

    private final HotbarObjectGroup defaultHotbarObjectGroup;

    private boolean visible;

    /**
     * Creates a hotbar profile based on a player
     * @param player The player to manage
     */
    public HotbarProfile(Player player) {
        this.player = player;

        defaultHotbarObjectGroup = new HotbarObjectGroup(player, new HashSet<>(){{
            for (int i = 0; i <= 8; i++) {
                add(i);
            }
        }}) {
            @Override
            public Integer getNextEmptySlot() {
                if (getHotbarObjectMap().isEmpty()) {
                    return null;
                } else {
                    return getHotbarObjectMap().keySet().iterator().next();
                }
            }
        };

        hotbarObjectGroupMap.put(DEFAULT_HOTBAR_OBJECT_GROUP_KEY, defaultHotbarObjectGroup);
    }

    /**
     * Sets the visibility of the profile
     * @param visible Whether or not the profile should be visible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        for (HotbarObjectGroup hotbarObjectGroup : hotbarObjectGroupMap.values()) {
            hotbarObjectGroup.setVisible(visible);
        }
    }

    /**
     * Adds an item
     * @param slot The slot to add the item to
     * @param itemStack The item stack to add
     */
    public void addItem(int slot, @Nullable ItemStack itemStack) {
        addHotbarObject(slot, new HotbarObject(player, slot, itemStack));
    }

    /**
     * Changes the ownership of a slot to a new hotbar object group
     * @param slot The slot to swap
     * @param newHotbarObjectGroup The new hotbar object group
     */
    public void swapSlotOwnership(int slot, @NotNull HotbarObjectGroup newHotbarObjectGroup) {
        for (HotbarObjectGroup otherHotbarObjectGroup : hotbarObjectGroupMap.values()) {
            if (otherHotbarObjectGroup.getHotbarObjectMap().containsKey(slot)) {
                otherHotbarObjectGroup.remove(slot, false);
                newHotbarObjectGroup.addHotbarObject(slot);
                return;
            }
        }

        throw new IllegalArgumentException(String.format("The HotbarProfile does not manage slot %d, so we can't swap" +
                " its ownership!", slot));
    }

    /**
     * Changes the ownership of a slot to a new hotbar object group
     * @param slot The slot to swap
     * @param newHotbarObjectGroupName The name of the new hotbar object group
     */
    public void swapSlotOwnership(int slot, String newHotbarObjectGroupName) {
        HotbarObjectGroup hotbarObjectGroup = hotbarObjectGroupMap.get(newHotbarObjectGroupName);
        swapSlotOwnership(slot, hotbarObjectGroup);
    }

    /**
     * Adds a hotbar object
     * @param slot The slot to add the hotbar object to
     * @param hotbarObject The hotbar object to add
     */
    public void addHotbarObject(int slot, HotbarObject hotbarObject) {
        for (HotbarObjectGroup hotbarObjectGroup : hotbarObjectGroupMap.values()) {
            if (hotbarObjectGroup.getHotbarObjectMap().containsKey(slot)) {
                hotbarObjectGroup.setHotbarObject(slot, hotbarObject);
                return;
            }
        }

        throw new IllegalArgumentException(String.format("The HotbarProfile does not manage slot %d, so we can't add " +
                "a hotbar object!", slot));
    }

    /**
     * Removes a hotbar object
     * @param slot The slot of the hotbar object
     * @param replace Whether or not the hotbar object group should replace the object in the slot and still manage it
     */
    public void removeHotbarObject(int slot, boolean replace) {
        for (HotbarObjectGroup hotbarObjectGroup : hotbarObjectGroupMap.values()) {
            if (hotbarObjectGroup.getHotbarObjectMap().containsKey(slot)) {
                hotbarObjectGroup.remove(slot, replace);
                if (!replace) {
                    defaultHotbarObjectGroup.addHotbarObject(slot);
                }
                return;
            }
        }

        throw new IllegalArgumentException(String.format("The HotbarProfile does not manage slot %d, so we can't " +
                "remove a hotbar object!", slot));
    }

    /**
     * Adds a hotbar object group
     * @param name The associated name of the hotbar object group
     * @param hotbarObjectGroup The hotbar object group to add
     */
    public void addHotbarObjectGroup(String name, HotbarObjectGroup hotbarObjectGroup) {
        if (!hotbarObjectGroupMap.containsKey(name)) {
            Set<Integer> defaultHotbarObjectGroupSlots = defaultHotbarObjectGroup.getHotbarObjectMap().keySet(),
                    newHotbarObjectGroupSlots = hotbarObjectGroup.getHotbarObjectMap().keySet();
            for (Integer slot : newHotbarObjectGroupSlots) {
                if (!defaultHotbarObjectGroupSlots.contains(slot)) {
                    throw new IllegalArgumentException(String.format("Cannot add HotbarObjectGroup to slot %d " +
                            "because of a preexisting HotbarObjectGroup", slot));
                }
            }

            for (Integer slot : newHotbarObjectGroupSlots) {
                defaultHotbarObjectGroup.remove(slot, false);
            }
            hotbarObjectGroupMap.put(name, hotbarObjectGroup);
            hotbarObjectGroup.setVisible(visible);
        } else {
            throw new IllegalArgumentException(String.format("HotbarObjectGroup %s already exists!", name));
        }
    }

    /**
     * Removes a hotbar object group
     * @param name The associated name of the hotbar object group
     */
    public void removeHotbarObjectGroup(String name) {
        if (!name.equals(DEFAULT_HOTBAR_OBJECT_GROUP_KEY)) {
            if (hotbarObjectGroupMap.containsKey(name)) {
                HotbarObjectGroup hotbarObjectGroup = hotbarObjectGroupMap.remove(name);
                hotbarObjectGroup.remove();
                for (Integer slot : hotbarObjectGroup.getHotbarObjectMap().keySet()) {
                    defaultHotbarObjectGroup.addHotbarObject(slot);
                }
            } else {
                throw new IllegalArgumentException(String.format("The HotbarProfile does not contain the " +
                        "HotbarObjectGroup %s!", name));
            }
        } else {
            throw new IllegalArgumentException("Cannot remove the default HotbarObjectGroup of a HotbarProfile!");
        }
    }

    /**
     * Gets a hotbar object group
     * @param name The name of the hotbar object group
     * @return The hotbar object group
     */
    public HotbarObjectGroup getHotbarObjectGroup(String name) {
        return hotbarObjectGroupMap.get(name);
    }

    /**
     * Gets a hotbar object group
     * @param slot The slot to get the hotbar object group from
     * @return The hotbar object group
     */
    public HotbarObjectGroup getHotbarObjectGroup(int slot) {
        for (HotbarObjectGroup hotbarObjectGroup : hotbarObjectGroupMap.values()) {
            HotbarObject hotbarObject = hotbarObjectGroup.getHotbarObject(slot);
            if (hotbarObject != null) {
                return hotbarObjectGroup;
            }
        }

        return null;
    }

    /**
     * Gets a hotbar object
     * @param slot The slot to get the hotbar object from
     * @return The hotbar object
     */
    public HotbarObject getHotbarObject(int slot) {
        for (HotbarObjectGroup hotbarObjectGroup : hotbarObjectGroupMap.values()) {
            HotbarObject hotbarObject = hotbarObjectGroup.getHotbarObject(slot);
            if (hotbarObject != null) {
                return hotbarObject;
            }
        }

        return null;
    }

    /**
     * Gets the next free slot in the hotbar profile
     * @return The next free slot
     */
    public Integer getNextFreeSlot() {
        return defaultHotbarObjectGroup.getNextEmptySlot();
    }

}
