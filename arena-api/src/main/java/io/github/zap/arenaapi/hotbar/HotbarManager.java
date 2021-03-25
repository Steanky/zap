package io.github.zap.arenaapi.hotbar;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the hotbar of a player
 */
public class HotbarManager {

    public final static String DEFAULT_PROFILE_NAME = "Default";

    private final Map<String, HotbarProfile> profiles = new HashMap<>();

    private final Player player;

    @Getter
    private HotbarProfile current = null;

    /**
     * Creates the hotbar manager of a player
     * @param player The player the hotbar manager manages
     */
    public HotbarManager(Player player) {
        this.player = player;

        switchProfile(DEFAULT_PROFILE_NAME);
    }

    public Map<String, HotbarProfile> getProfiles() {
        return Collections.unmodifiableMap(profiles);
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
     * @param slot The slot to add the item to
     * @param itemStack The item stack to add
     */
    public void addItem(int slot, ItemStack itemStack) {
        addItem(current, slot, itemStack);
    }

    /**
     * Adds an item to a hotbar profile
     * @param hotbarProfile The hotbar profile to add the item to
     * @param slot The slot to add the item to
     * @param itemStack The item stack to add
     */
    public void addItem(HotbarProfile hotbarProfile, int slot, ItemStack itemStack) {
        hotbarProfile.addItem(slot, itemStack);
    }

    /**
     * Sets a hotbar object in the current profile
     * @param slot The slot to set the hotbar object in
     * @param hotbarObject The hotbar object to add
     */
    public void setHotbarObject(int slot, HotbarObject hotbarObject) {
        setHotbarObject(current, slot, hotbarObject);
    }

    /**
     * Sets a hotbar object in a hotbar profile
     * @param hotbarProfile The hotbar profile to set the hotbar object in
     * @param slot The slot to set the hotbar object in
     * @param hotbarObject The hotbar object to add
     */
    public void setHotbarObject(HotbarProfile hotbarProfile, int slot, HotbarObject hotbarObject) {
        hotbarProfile.addHotbarObject(slot, hotbarObject);
    }

    /**
     * Removes a hotbar object from the current profile
     * @param slot The slot of the hotbar object
     * @param replace Whether or not the internal hotbar object group should replace the object in the slot and still
     *                manage it
     */
    public void removeHotbarObject(int slot, boolean replace) {
        removeHotbarObject(current, slot, replace);
    }

    /**
     * Removes a hotbar object from a hotbar profile
     * @param hotbarProfile The hotbar profile to remove the hotbar object from
     * @param slot The slot of the hotbar object
     * @param replace Whether or not the internal hotbar object group should replace the object in the slot and still
     *                manage it
     */
    public void removeHotbarObject(HotbarProfile hotbarProfile, int slot, boolean replace) {
        hotbarProfile.removeHotbarObject(slot, replace);
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
     * @param slot The slot to get the hotbar object from
     * @return The hotbar object
     */
    public HotbarObject getHotbarObject(int slot) {
        return getHotbarObject(current, slot);
    }

    /**
     * Gets a hotbar object group in the current profile
     * @param name The name of the hotbar object group
     * @return The hotbar object group
     */
    public HotbarObjectGroup getHotbarObjectGroup(String name) {
        return getHotbarObjectGroup(current, name);
    }

    /**
     * Gets a hotbar object group in a hotbar profile
     * @param hotbarProfile The hotbar profile to get the hotbar object group from
     * @param name The name of the hotbar object group
     * @return The hotbar object group
     */
    public HotbarObjectGroup getHotbarObjectGroup(HotbarProfile hotbarProfile, String name) {
        return hotbarProfile.getHotbarObjectGroup(name);
    }

    /**
     * Gets a hotbar object in a hotbar profile
     * @param hotbarProfile The hotbar profile to get the hotbar object from
     * @param slot The slot to get the hotbar object from
     * @return The hotbar object
     */
    public HotbarObject getHotbarObject(HotbarProfile hotbarProfile, int slot) {
        return hotbarProfile.getHotbarObject(slot);
    }

    /**
     * Gets the currently selected hotbar object
     * @return The currently selected hotbar object
     */
    public HotbarObject getSelectedObject() {
        return current.getHotbarObject(player.getInventory().getHeldItemSlot());
    }

    /**
     * Method to call when different slot is selected in the hotbar
     * @param slot The selected slot
     */
    public void setSelectedSlot(int slot) {
        getSelectedObject().onSlotDeselected();
        getHotbarObject(slot).onSlotSelected();
    }

    /**
     * Method to call when the slot is clicked in the hotbar
     */
    public void click(Action action) {
        HotbarObject hotbarObject = getSelectedObject();

        if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
            hotbarObject.onLeftClick(action);
        } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            hotbarObject.onRightClick(action);
        }
    }

}
