package io.github.zap.zombies.game.equipment.gun;

import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.zombies.game.equipment.EquipmentType;
import io.github.zap.zombies.game.equipment.UpgradeableEquipmentObjectGroup;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

/**
 * Object group of guns
 */
public class GunObjectGroup extends UpgradeableEquipmentObjectGroup {

    public GunObjectGroup(Player player, Set<Integer> slots) {
        super(player, slots);
    }

    @Override
    public ItemStack createPlaceholderItemStack(int placeholderNumber) {
        return null;
    }

    @Override
    public boolean isObjectRecommendedEquipment(HotbarObject hotbarObject) {
        return hotbarObject instanceof Gun<?, ?>;
    }

    @Override
    public String getEquipmentType() {
        return EquipmentType.GUN.name();
    }

}
