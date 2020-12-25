package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.zombies.game.data.equipment.PerkData;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import org.bukkit.entity.Player;

/**
 * Represents a perk hotbar equipment
 */
public class PerkEquipment extends UpgradeableEquipment<PerkData> {
    public PerkEquipment(Player player, int slotId, PerkData equipmentData) {
        super(player, slotId, equipmentData);
    }
}
