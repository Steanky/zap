package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.jetbrains.annotations.NotNull;

/**
 * Data for the extra weapon perk
 */
public class ExtraWeaponData extends PerkData<ExtraWeaponLevel> {

    @Override
    public @NotNull String getEquipmentType() {
        return EquipmentType.EXTRA_WEAPON.name();
    }

}
