package io.github.zap.zombies.game.equipment.melee;

import io.github.zap.zombies.game.data.equipment.MeleeData;
import io.github.zap.zombies.game.equipment.Equipment;
import org.bukkit.entity.Player;

/**
 * Represents a weapon that uses melee combat
 */
public class MeleeWeapon extends Equipment<MeleeData> {
    public MeleeWeapon(Player player, int slotId, MeleeData equipmentData) {
        super(player, slotId, equipmentData);
    }
}
