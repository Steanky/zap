package io.github.zap.zombies.game.equipment.melee;

import io.github.zap.zombies.game.data.equipment.melee.MeleeData;
import io.github.zap.zombies.game.data.equipment.melee.MeleeLevel;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import org.bukkit.entity.Player;

/**
 * Represents a weapon that uses melee combat
 */
public class MeleeWeapon extends UpgradeableEquipment<MeleeData, MeleeLevel> {
    public MeleeWeapon(Player player, int slotId, MeleeData equipmentData) {
        super(player, slotId, equipmentData);
    }
}
