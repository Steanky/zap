package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.jetbrains.annotations.NotNull;

/**
 * Data for the fast revive perk
 */
public class FastReviveData extends PerkData<FastReviveLevel> {

    @Override
    public @NotNull String getEquipmentType() {
        return EquipmentType.FAST_REVIVE.name();
    }

}
