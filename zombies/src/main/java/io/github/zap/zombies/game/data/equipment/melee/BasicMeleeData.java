package io.github.zap.zombies.game.data.equipment.melee;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.jetbrains.annotations.NotNull;

/**
 * Data for a basic melee weapon
 */
public class BasicMeleeData extends MeleeData<BasicMeleeLevel> {

    @Override
    public @NotNull String getEquipmentType() {
        return EquipmentType.BASIC_MELEE.name();
    }

}
