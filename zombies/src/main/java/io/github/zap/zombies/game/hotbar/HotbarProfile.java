package io.github.zap.zombies.game.hotbar;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The profile of a player that can be swapped between when the player is in different states
 */
public class HotbarProfile {

    private final static String DEFAULT_HOTBAR_OBJECT_GROUP_KEY = "Default";

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
        }});
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
     * @param slotId The slot to add the item to
     * @param itemStack The item stack to add
     */
    public void addItem(int slotId, ItemStack itemStack) {
        addHotbarObject(slotId, new HotbarObject(player, slotId, itemStack));
    }

    /**
     * Adds a hotbar object
     * @param slotId The slot to add the hotbar object to
     * @param hotbarObject The hotbar object to add
     */
    public void addHotbarObject(int slotId, HotbarObject hotbarObject) {
        for (HotbarObjectGroup hotbarObjectGroup : hotbarObjectGroupMap.values()) {
            if (hotbarObjectGroup.getSlots().contains(slotId)) {
                hotbarObjectGroup.setHotbarObject(slotId, hotbarObject);
                return;
            }
        }

        throw new IllegalArgumentException(String.format("The HotbarProfile does not manage slot %d, so we can't add " +
                "a hotbar object!", slotId));
    }

    /**
     * Removes a hotbar object
     * @param slotId The slot of the hotbar object
     * @param replace Whether or not the hotbar object group should replace the object in the slot and still manage it
     */
    public void removeHotbarObject(int slotId, boolean replace) {
        for (HotbarObjectGroup hotbarObjectGroup : hotbarObjectGroupMap.values()) {
            if (hotbarObjectGroup.getSlots().contains(slotId)) {
                hotbarObjectGroup.remove(slotId, replace);
                if (!replace) {
                    defaultHotbarObjectGroup.addHotbarObject(slotId);
                }
                return;
            }
        }

        throw new IllegalArgumentException(String.format("The HotbarProfile does not manage slot %d, so we can't " +
                "remove a hotbar object!", slotId));
    }

    /**
     * Adds a hotbar object group
     * @param name The associated name of the hotbar object group
     * @param hotbarObjectGroup The hotbar object group to add
     */
    public void addHotbarObjectGroup(String name, HotbarObjectGroup hotbarObjectGroup) {
        if (!hotbarObjectGroupMap.containsKey(name)) {
            Set<Integer> defaultHotbarObjectGroupSlots = defaultHotbarObjectGroup.getSlots(),
                    newHotbarObjectGroupSlots = hotbarObjectGroup.getSlots();
            for (Integer slotId : newHotbarObjectGroupSlots) {
                if (!defaultHotbarObjectGroupSlots.contains(slotId)) {
                    throw new IllegalArgumentException(String.format("Cannot add HotbarObjectGroup to slotId %d " +
                            "because of a preexisting HotbarObjectGroup", slotId));
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
                for (Integer slotId : hotbarObjectGroup.getSlots()) {
                    defaultHotbarObjectGroup.addHotbarObject(slotId);
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
     * Gets a hotbar object
     * @param slotId The slot to get the hotbar object from
     * @return The hotbar object
     */
    public HotbarObject getHotbarObject(int slotId) {
        for (HotbarObjectGroup hotbarObjectGroup : hotbarObjectGroupMap.values()) {
            HotbarObject hotbarObject = hotbarObjectGroup.getHotbarObject(slotId);
            if (hotbarObject != null) {
                return hotbarObject;
            }
        }

        return null;
    }

}
