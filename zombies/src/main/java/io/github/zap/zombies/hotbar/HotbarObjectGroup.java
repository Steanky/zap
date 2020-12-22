package io.github.zap.zombies.hotbar;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A group of hotbar object managed together
 */
public class HotbarObjectGroup {

    protected final Map<Integer, HotbarObject> hotbarObjectMap = new HashMap<>();

    /**
     * Creates a hotbar object group
     * @param player The player the hotbar object group belongs to
     * @param slots The slots that the hotbar object group manages
     */
    public HotbarObjectGroup(Player player, Set<Integer> slots) {
        for (Integer slot : slots) {
            hotbarObjectMap.put(slot, new HotbarObject(player, slot));
        }
    }

    /**
     * Sets the visibility of the hotbar object group
     * @param visible Whether or not the hotbar object group is visible
     */
    public void setVisible(boolean visible) {
        for (HotbarObject hotbarObject : hotbarObjectMap.values()) {
            if (hotbarObject != null) {
                hotbarObject.setVisible(visible);
            }
        }
    }

    /**
     * Removes and no longer manages a slot in a hotbar object group
     * @param slotId The slot to remove
     */
    public void remove(int slotId) {
        if (hotbarObjectMap.containsKey(slotId)) {
            HotbarObject remove = hotbarObjectMap.remove(slotId);
            remove.remove();
        } else {
            throw new IllegalArgumentException(String.format("The HotbarObjectGroup does not manage slot %s!", slotId));
        }
    }

    /**
     * Removes all hotbar objects and no longer manages any
     */
    public void remove() {
        for (HotbarObject hotbarObject : hotbarObjectMap.values()) {
            if (hotbarObject != null) {
                hotbarObject.remove();
            }
        }
    }

    /**
     * Gets the hotbar object in a slot
     * @param slotId The slot to get the hotbar object from
     * @return The hotbar object
     */
    public HotbarObject getHotbarObject(int slotId) {
        return hotbarObjectMap.get(slotId);
    }

    /**
     * Sets the hotbar object in a slot
     * @param slotId The slot to set the hotbar object in
     * @param hotbarObject The hotbar object to set
     */
    public void setHotbarObject(int slotId, HotbarObject hotbarObject) {
        if (hotbarObjectMap.containsKey(slotId)) {
            hotbarObjectMap.put(slotId, hotbarObject);
        } else {
            throw new IllegalArgumentException(String.format("The HotbarObjectGroup does not contain slotId %d!", slotId));
        }
    }

    /**
     * Gets all of the slots of the hotbar object group
     * @return The set of slots
     */
    public Set<Integer> getSlots() {
        return hotbarObjectMap.keySet();
    }

}
