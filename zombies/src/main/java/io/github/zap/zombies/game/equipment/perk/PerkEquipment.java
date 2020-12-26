package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.zombies.game.data.equipment.perk.PerkData;
import io.github.zap.zombies.game.data.equipment.perk.PerkLevel;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import org.bukkit.entity.Player;

/**
 * Represents a perk hotbar equipment
 */
public class PerkEquipment extends UpgradeableEquipment<PerkData, PerkLevel> {
    public PerkEquipment(Player player, int slot, PerkData equipmentData) {
        super(player, slot, equipmentData);
    }
}
