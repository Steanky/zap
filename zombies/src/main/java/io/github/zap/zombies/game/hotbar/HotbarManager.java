package io.github.zap.zombies.game.hotbar;

import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import io.github.zap.zombies.game.equipment.EquipmentType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the Hotbar of a player
 */
public class HotbarManager {

    public final static String DEFAULT_PROFILE_NAME = "Default";

    public final static String KNOCKED_DOWN_PROFILE_NAME = "Knocked";

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
     * Sets a hotbar object in the current profile
     * @param slotId The slot to set the hotbar object in
     * @param hotbarObject The hotbar object to add
     */
    public void setHotbarObject(int slotId, HotbarObject hotbarObject) {
        setHotbarObject(current, slotId, hotbarObject);
    }

    /**
     * Sets a hotbar object in a hotbar profile
     * @param hotbarProfile The hotbar profile to set the hotbar object in
     * @param slotId The slot to set the hotbar object in
     * @param hotbarObject The hotbar object to add
     */
    public void setHotbarObject(HotbarProfile hotbarProfile, int slotId, HotbarObject hotbarObject) {
        hotbarProfile.addHotbarObject(slotId, hotbarObject);
    }

    /**
     * Removes a hotbar object from the current profile
     * @param slotId The slot of the hotbar object
     * @param replace Whether or not the internal hotbar object group should replace the object in the slot and still
     *                manage it
     */
    public void removeHotbarObject(int slotId, boolean replace) {
        removeHotbarObject(current, slotId, replace);
    }

    /**
     * Removes a hotbar object from a hotbar profile
     * @param hotbarProfile The hotbar profile to remove the hotbar object from
     * @param slotId The slot of the hotbar object
     * @param replace Whether or not the internal hotbar object group should replace the object in the slot and still
     *                manage it
     */
    public void removeHotbarObject(HotbarProfile hotbarProfile, int slotId, boolean replace) {
        hotbarProfile.removeHotbarObject(slotId, replace);
    }

    /**
     * Adds an equipment object group to the current profile
     * @param equipmentObjectGroup The equipment object group to add
     */
    public void addEquipmentObjectGroup(EquipmentObjectGroup equipmentObjectGroup) {
        addEquipmentObjectGroup(current, equipmentObjectGroup);
    }

    /**
     * Adds an equipment object group to a hotbar profile
     * @param equipmentObjectGroup The equipment object group to add
     */
    public void addEquipmentObjectGroup(HotbarProfile hotbarProfile, EquipmentObjectGroup equipmentObjectGroup) {
        hotbarProfile.addHotbarObjectGroup(equipmentObjectGroup.getEquipmentType(), equipmentObjectGroup);
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
     * Removes an equipment object group from the current profile
     * @param equipmentType The type of equipment group to remove
     */
    public void removeHotbarObjectGroup(EquipmentType equipmentType) {
        removeHotbarObjectGroup(current, equipmentType);
    }

    /**
     * Removes an equipment object group from a hotbar profile
     * @param hotbarProfile The hotbar profile to remove the hotbar object group from
     * @param equipmentType The type of equipment group to remove
     */
    public void removeHotbarObjectGroup(HotbarProfile hotbarProfile, EquipmentType equipmentType) {
        hotbarProfile.removeHotbarObjectGroup(equipmentType.toString());
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
     * Gets an equipment object group in the current profile
     * @param equipmentType The type of the equipment of the equipment object group
     * @return The equipment object group
     */
    public HotbarObjectGroup getEquipmentObjectGroup(EquipmentType equipmentType) {
        return getEquipmentGroup(current, equipmentType);
    }

    /**
     * Gets an equipment object group in a hotbar profile
     * @param hotbarProfile The hotbar profile to get the equipment object group from
     * @param equipmentType type of the equipment of the equipment object group
     * @return The equipment object group
     */
    public HotbarObjectGroup getEquipmentGroup(HotbarProfile hotbarProfile, EquipmentType equipmentType) {
        return hotbarProfile.getHotbarObjectGroup(equipmentType.toString());
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
        return current.getHotbarObjectGroup(name);
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

    /**
     * Method to call when different slot is selected in the hotbar
     * @param slotId The selected slot
     */
    public void setSelectedSlot(int slotId) {
        getSelectedObject().onSlotDeselected();
        getHotbarObject(slotId).onSlotSelected();
    }

    /**
     * Method to call when the slot is clicked in the hotbar
     */
    public void click(Action action) {
        HotbarObject hotbarObject = getSelectedObject();
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            hotbarObject.onLeftClick();
        } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            hotbarObject.onRightClick();
        }
    }

}
