package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import io.github.zap.zombies.game.equipment.EquipmentType;
import io.github.zap.zombies.game.hotbar.HotbarObject;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

/**
 * Object group of perks
 */
public class PerkObjectGroup extends EquipmentObjectGroup {
    public PerkObjectGroup(Player player, Set<Integer> slots) {
        super(player, slots);
    }

    @Override
    public ItemStack createPlaceholderItemStack(int placeholderNumber) {
        return null;
    }

    @Override
    public boolean isObjectRecommendedEquipment(HotbarObject hotbarObject) {
        return hotbarObject instanceof PerkEquipment;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.PERK;
    }
}
