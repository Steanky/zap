package io.github.zap.zombies.game.equipment.melee;

import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import io.github.zap.zombies.game.equipment.EquipmentType;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import io.github.zap.zombies.game.equipment.UpgradeableEquipmentObjectGroup;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Object group of melee weapons
 */
public class MeleeObjectGroup extends UpgradeableEquipmentObjectGroup {

    public MeleeObjectGroup(Player player, Set<Integer> slots) {
        super(player, slots);
    }

    @Override
    public ItemStack createPlaceholderItemStack(int placeholderNumber) {
        return null;
    }

    @Override
    public boolean isObjectRecommendedEquipment(HotbarObject hotbarObject) {
        return hotbarObject instanceof MeleeWeapon;
    }

    @Override
    public String getEquipmentType() {
        return EquipmentType.MELEE.name();
    }
}
