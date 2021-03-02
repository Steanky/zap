package io.github.zap.zombies.game.equipment.melee;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.melee.MeleeData;
import io.github.zap.zombies.game.data.equipment.melee.MeleeLevel;
import io.github.zap.zombies.game.equipment.Ultimateable;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;

/**
 * Represents a weapon that uses melee combat
 */
public class MeleeWeapon extends UpgradeableEquipment<MeleeData, MeleeLevel> implements Ultimateable {
    public MeleeWeapon(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer, int slot, MeleeData equipmentData) {
        super(zombiesArena, zombiesPlayer, slot, equipmentData);
    }
}
