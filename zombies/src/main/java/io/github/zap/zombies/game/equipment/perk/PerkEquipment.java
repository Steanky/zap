package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.perk.PerkData;
import io.github.zap.zombies.game.data.equipment.perk.PerkLevel;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;

/**
 * Represents a perk hotbar equipment
 */
public class PerkEquipment extends UpgradeableEquipment<PerkData, PerkLevel> {
    public PerkEquipment(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer, int slot, PerkData equipmentData) {
        super(zombiesArena, zombiesPlayer, slot, equipmentData);
    }
}
