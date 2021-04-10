package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.jetbrains.annotations.NotNull;

/**
 * Data for the frozen bullets perk
 */
public class FrozenBulletsData extends PerkData<FrozenBulletsLevel> {

    @Override
    public @NotNull String getEquipmentType() {
        return EquipmentType.FROZEN_BULLETS.name();
    }

}
