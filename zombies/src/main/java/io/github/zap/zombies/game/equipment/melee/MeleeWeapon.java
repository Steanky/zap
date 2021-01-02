package io.github.zap.zombies.game.equipment.melee;

import io.github.zap.zombies.game.data.equipment.melee.MeleeData;
import io.github.zap.zombies.game.data.equipment.melee.MeleeLevel;
import io.github.zap.zombies.game.equipment.Ultimateable;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import org.bukkit.entity.Player;

/**
 * Represents a weapon that uses melee combat
 */
public class MeleeWeapon extends UpgradeableEquipment<MeleeData, MeleeLevel> implements Ultimateable {
    public MeleeWeapon(Player player, int slot, MeleeData equipmentData) {
        super(player, slot, equipmentData);
    }
}
