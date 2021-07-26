package io.github.zap.arenaapi.hotbar;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public HotbarObjectGroup(@NotNull Player player, @NotNull Set<Integer> slots) {
        this.player = player;

        for (Integer slot : slots) {
            hotbarObjectMap.put(slot, createDefaultHotbarObject(player, slot));
        }
    }

    /**
     * Creates a default hotbar object to insert into new slots
     * @param player The player the hotbar object belogns to
     * @param slot The slot of the hotbar object
     * @return The new hotbar object
     */
    public @NotNull HotbarObject createDefaultHotbarObject(@NotNull Player player, int slot) {
        return new HotbarObject(player, slot);
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
     * @param slot The slot to remove
     * @param replace Whether or not the hotbar object group should replace the object in the slot and still manage it
     */
    public void remove(int slot, boolean replace) {
        if (hotbarObjectMap.containsKey(slot)) {
            HotbarObject remove = (replace) ? hotbarObjectMap.get(slot) : hotbarObjectMap.remove(slot);
            remove.remove();

            if (replace) {
                setHotbarObject(slot, createDefaultHotbarObject(player, slot));
            }
        } else {
            throw new IllegalArgumentException(String.format("The HotbarObjectGroup does not manage slot %s!", slot));
        }
    }

    /**
     * Removes all hotbar objects and no longer manages any
     */
    public void remove() {
        for (Map.Entry<Integer, HotbarObject> slot : hotbarObjectMap.entrySet()) {
            slot.getValue().remove();
            hotbarObjectMap.remove(slot.getKey());
        }
    }

    /**
     * Adds a new empty hotbar object to a new slot.
     * This should not be used to set hotbar objects
     * @param slot The slot to add the empty hotbar object to
     */
    public void addHotbarObject(int slot) {
        addHotbarObject(slot, createDefaultHotbarObject(player, slot));
    }

    /**
     * Adds a new hotbar object to a new slot.
     * This should not be used to set hotbar objects
     * @param slot The slot to add the hotbar object to
     * @param hotbarObject The hotbar object to add
     */
    public void addHotbarObject(int slot, @NotNull HotbarObject hotbarObject) {
        Map<Integer, HotbarObject> hotbarObjectMap = getHotbarObjectMap();
        if (!hotbarObjectMap.containsKey(slot)) {
            hotbarObjectMap.put(slot, hotbarObject);
            hotbarObject.setVisible(visible);
        } else {
            throw new IllegalArgumentException(String.format("The HotbarObjectGroup already contains slot " +
                    "%d! (Did you mean to use setHotbarObject?)", slot));
        }
    }

    /**
     * Gets the hotbar object in a slot
     * @param slot The slot to get the hotbar object from
     * @return The hotbar object
     */
    public @Nullable HotbarObject getHotbarObject(int slot) {
        return hotbarObjectMap.get(slot);
    }

    /**
     * Sets the hotbar object in a slot
     * @param slot The slot to set the hotbar object in
     * @param hotbarObject The hotbar object to set
     */
    public void setHotbarObject(int slot, @NotNull HotbarObject hotbarObject) {
        if (hotbarObjectMap.containsKey(slot)) {
            if (hotbarObject.getSlot() != slot) {
                throw new IllegalArgumentException(String.format("Attempted to put a hotbar object that goes in slot " +
                        "%d in slot %d!", hotbarObject.getSlot(), slot));
            }

            remove(slot, false);
            hotbarObjectMap.put(slot, hotbarObject);

            if (visible) {
                hotbarObject.setVisible(true);
            }
        } else {
            throw new IllegalArgumentException(String.format("The HotbarObjectGroup does not contain slot " +
                    "%d! (Did you mean to use addHotbarObject?)", slot));
        }
    }

    /**
     * Gets the next available slot to put an object in according to the hotbar object group's functionality
     */
    public @Nullable Integer getNextEmptySlot() {
        return null;
    }

}
