package io.github.zap.zombies.game.hotbar;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the Hotbar of a player
 */
public class HotbarManager {

    private final static String DEFAULT_PROFILE_NAME = "Default";

    private final Map<String, HotbarProfile> profiles = new HashMap<>();

    private final Player player;

    private HotbarProfile current = null;

    /**
     * Creates the hotbar manager of a player
     * @param player The player the hotbar manager manages
     */
    public HotbarManager(Player player) {
        this.player = player;

        switchProfile(DEFAULT_PROFILE_NAME);
    }

    /**
     * Switches the HotbarManager profile and creates one if it doesn't exist
     * @param name The name of the HotbarManager profile
     */
    public void switchProfile(String name) {
        if (current != null) {
            current.setVisible(false);
        }

        current = profiles.computeIfAbsent(name, (String s) -> new HotbarProfile(player));
        current.setVisible(true);
    }

    /**
     * Adds an item to the current profile
     * @param slotId The slot to add the item to
     * @param itemStack The item stack to add
     */
    public void addItem(int slotId, ItemStack itemStack) {
        addItem(current, slotId, itemStack);
    }

    /**
     * Adds an item to a hotbar profile
     * @param hotbarProfile The hotbar profile to add the item to
     * @param slotId The slot to add the item to
     * @param itemStack The item stack to add
     */
    public void addItem(HotbarProfile hotbarProfile, int slotId, ItemStack itemStack) {
        hotbarProfile.addItem(slotId, itemStack);
    }

    /**
     * Adds a hotbar object to the current profile
     * @param slotId The slot to add the hotbar object to
     * @param hotbarObject The hotbar object to add
     */
    public void addHotbarObject(int slotId, HotbarObject hotbarObject) {
        addHotbarObject(current, slotId, hotbarObject);
    }

    /**
     * Adds a hotbar object to a hotbar profile
     * @param hotbarProfile The hotbar profile to add the hotbar object to
     * @param slotId The slot to add the item to
     * @param hotbarObject The hotbar object to add
     */
    public void addHotbarObject(HotbarProfile hotbarProfile, int slotId, HotbarObject hotbarObject) {
        hotbarProfile.addHotbarObject(slotId, hotbarObject);
    }

    /**
     * Removes a hotbar object from the current profile
     * @param slotId The slot of the hotbar object
     */
    public void removeHotbarObject(int slotId) {
        removeHotbarObject(current, slotId);
    }

    /**
     * Removes a hotbar object from a hotbar profile
     * @param hotbarProfile The hotbar profile to remove the hotbar object from
     * @param slotId The slot of the hotbar object
     */
    public void removeHotbarObject(HotbarProfile hotbarProfile, int slotId) {
        hotbarProfile.removeHotbarObject(slotId);
    }

    /**
     * Adds a hotbar object group to the current profile
     * @param name The associated name of the hotbar object group
     * @param hotbarObjectGroup The hotbar object group to add
     */
    public void addHotbarObjectGroup(String name, HotbarObjectGroup hotbarObjectGroup) {
        addHotbarObjectGroup(current, name, hotbarObjectGroup);
    }

    /**
     * Adds a hotbar object group to a hotbar profile
     * @param hotbarProfile The hotbar profile to add the hotbar object group to
     * @param name The associated name of the hotbar object group
     * @param hotbarObjectGroup The hotbar object group to add
     */
    public void addHotbarObjectGroup(HotbarProfile hotbarProfile, String name, HotbarObjectGroup hotbarObjectGroup) {
        hotbarProfile.addHotbarObjectGroup(name, hotbarObjectGroup);
    }

    /**
     * Removes a hotbar object group from the current profile
     * @param name The associated name of the hotbar object group
     */
    public void removeHotbarObjectGroup(String name) {
        removeHotbarObjectGroup(current, name);
    }

    /**
     * Removes a hotbar object group from a hotbar profile
     * @param hotbarProfile The hotbar profile to remove the hotbar object group from
     * @param name The associated name of the hotbar object group
     */
    public void removeHotbarObjectGroup(HotbarProfile hotbarProfile, String name) {
        hotbarProfile.removeHotbarObjectGroup(name);
    }

    /**
     * Gets a hotbar object in the current profile
     * @param slotId The slot to get the hotbar object from
     * @return The hotbar object
     */
    public HotbarObject getHotbarObject(int slotId) {
        return getHotbarObject(current, slotId);
    }

    /**
     * Gets a hotbar object in a hotbar profile
     * @param hotbarProfile The hotbar profile to get the hotbar object from
     * @param slotId The slot to get the hotbar object from
     * @return The hotbar object
     */
    public HotbarObject getHotbarObject(HotbarProfile hotbarProfile, int slotId) {
        return hotbarProfile.getHotbarObject(slotId);
    }

    /**
     * Gets the currently selected hotbar object
     * @return The currently selected hotbar object
     */
    public HotbarObject getSelectedObject() {
        return current.getHotbarObject(player.getInventory().getHeldItemSlot());
    }

}
