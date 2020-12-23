package io.github.zap.zombies.game.hotbar;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A group of hotbar object managed together
 */
public class HotbarObjectGroup {

    @Getter
    private final Map<Integer, HotbarObject> hotbarObjectMap = new HashMap<>();

    @Getter
    private final Player player;

    @Getter
    private boolean visible = false;

    /**
     * Creates a hotbar object group
     * @param player The player the hotbar object group belongs to
     * @param slots The slots that the hotbar object group manages
     */
    public HotbarObjectGroup(Player player, Set<Integer> slots) {
        this.player = player;
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
        this.visible = visible;
    }

    /**
     * Removes a slot in a hotbar object group
     * @param slotId The slot to remove
     * @param replace Whether or not the hotbar object group should replace the object in the slot and still manage it
     */
    public void remove(int slotId, boolean replace) {
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
     * Adds a new empty hotbar object to a new slot.
     * This should not be used to set hotbar objects
     * @param slotId The slot to add the empty hotbar object to
     */
    public void addHotbarObject(int slotId) {
        addHotbarObject(slotId, new HotbarObject(player, slotId));
    }

    /**
     * Adds a new hotbar object to a new slot.
     * This should not be used to set hotbar objects
     * @param slotId The slot to add the hotbar object to
     * @param hotbarObject The hotbar object to add
     */
    public void addHotbarObject(int slotId, HotbarObject hotbarObject) {
        Map<Integer, HotbarObject> hotbarObjectMap = getHotbarObjectMap();
        if (!hotbarObjectMap.containsKey(slotId)) {
            hotbarObjectMap.put(slotId, hotbarObject);
            hotbarObject.setVisible(isVisible());
        } else {
            throw new IllegalArgumentException(String.format("The HotbarObjectGroup already contains slotId %d! (Did you mean to use setHotbarObject?)", slotId));
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

            if (visible) {
                hotbarObject.setVisible(true);
            }
        } else {
            throw new IllegalArgumentException(String.format("The HotbarObjectGroup does not contain slotId %d! (Did you mean to use addHotbarObject?)", slotId));
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
