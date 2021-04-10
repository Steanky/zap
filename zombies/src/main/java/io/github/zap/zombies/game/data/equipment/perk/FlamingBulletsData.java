package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.jetbrains.annotations.NotNull;

/**
 * Data for the flaming bullets perk
 */
public class FlamingBulletsData extends PerkData<FlamingBulletsLevel> {

    @Override
    public @NotNull String getEquipmentType() {
        return EquipmentType.FLAMING_BULLETS.name();
    }

}
