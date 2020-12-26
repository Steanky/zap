package io.github.zap.zombies.game.hotbar;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A group of hotbar object managed together
 */
@Getter
public class HotbarObjectGroup {

    private final Map<Integer, HotbarObject> hotbarObjectMap = new HashMap<>();

    private final Player player;

    private boolean visible = false;

    /**
     * Creates a hotbar object group
     * @param player The player the hotbar object group belongs to
     * @param slots The slots that the hotbar object group manages
     */
    public HotbarObjectGroup(Player player, Set<Integer> slots) {
        this.player = player;
        for (Integer slot : slots) {
            hotbarObjectMap.put(slot, createDefaultHotbarObject(player, slot));
        }
    }

    /**
     * Creates a default hotbar object to insert into new slots
     * @param player The player the hotbar object belogns to
     * @param slotId The slot of the hotbar object
     * @return The new hotbar object
     */
    public HotbarObject createDefaultHotbarObject(Player player, int slotId) {
        return new HotbarObject(player, slotId);
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
            HotbarObject remove;
            if (!replace) {
                remove = hotbarObjectMap.remove(slotId);
            } else {
                remove = hotbarObjectMap.get(slotId);
            }
            remove.remove();
        } else {
            throw new IllegalArgumentException(String.format("The HotbarObjectGroup does not manage slot %s!", slotId));
        }
    }

    /**
     * Removes all hotbar objects and no longer manages any
     */
    public void remove() {
        for (Map.Entry<Integer, HotbarObject> slotId : hotbarObjectMap.entrySet()) {
            slotId.getValue().remove();
            hotbarObjectMap.remove(slotId.getKey());
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
            throw new IllegalArgumentException(String.format("The HotbarObjectGroup already contains slotId " +
                    "%d! (Did you mean to use setHotbarObject?)", slotId));
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
            if (hotbarObject.getSlotId() != slotId) {
                throw new IllegalArgumentException(String.format("Attempted to put a hotbar object that goes in slot " +
                        "%d in slot %d!", hotbarObject.getSlotId(), slotId));
            }

            hotbarObjectMap.put(slotId, hotbarObject);

            if (visible) {
                hotbarObject.setVisible(true);
            }
        } else {
            throw new IllegalArgumentException(String.format("The HotbarObjectGroup does not contain slot " +
                    "%d! (Did you mean to use addHotbarObject?)", slotId));
        }
    }

    /**
     * Gets the next available slot to put an object in according to the hotbar object group's functionality
     */
    public Integer getNextEmptySlot() {
        return null;
    }

    /**
     * Gets all of the slots of the hotbar object group
     * @return The set of slots
     */
    public Set<Integer> getSlots() {
        return hotbarObjectMap.keySet();
    }

}
