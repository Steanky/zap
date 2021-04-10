package io.github.zap.zombies.game.data.equipment.melee;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.jetbrains.annotations.NotNull;

/**
 * Data for an AOE melee weapon
 */
public class AOEMeleeData extends MeleeData<AOEMeleeLevel> {

    @Override
    public @NotNull String getEquipmentType() {
        return EquipmentType.AOE_MELEE.name();
    }

}
