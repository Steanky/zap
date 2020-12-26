package io.github.zap.zombies.game.hotbar;

import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarProfile;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import org.bukkit.entity.Player;

/**
 * Extended hotbar manager with utility methods fo equipment object groups
 */
public class ZombiesHotbarManager extends HotbarManager {

    public final static String KNOCKED_DOWN_PROFILE_NAME = "Knocked";

    public ZombiesHotbarManager(Player player) {
        super(player);
    }

    /**
     * Adds an equipment object group to the current profile
     * @param equipmentObjectGroup The equipment object group to add
     */
    public void addEquipmentObjectGroup(EquipmentObjectGroup equipmentObjectGroup) {
        addEquipmentObjectGroup(getCurrent(), equipmentObjectGroup);
    }

    /**
     * Adds an equipment object group to a hotbar profile
     * @param equipmentObjectGroup The equipment object group to add
     */
    public void addEquipmentObjectGroup(HotbarProfile hotbarProfile, EquipmentObjectGroup equipmentObjectGroup) {
        hotbarProfile.addHotbarObjectGroup(equipmentObjectGroup.getEquipmentType(), equipmentObjectGroup);
    }
}
