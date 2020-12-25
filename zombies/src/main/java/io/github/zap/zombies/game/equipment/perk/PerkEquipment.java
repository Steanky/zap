package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.zombies.game.data.equipment.PerkData;
import io.github.zap.zombies.game.equipment.Equipment;
import org.bukkit.entity.Player;

/**
 * Represents a perk
 */
public class PerkEquipment extends Equipment<PerkData> {
    public PerkEquipment(Player player, int slotId, PerkData equipmentData) {
        super(player, slotId, equipmentData);
    }
}
