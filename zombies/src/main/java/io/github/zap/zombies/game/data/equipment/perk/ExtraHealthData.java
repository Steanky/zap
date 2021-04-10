package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.jetbrains.annotations.NotNull;

/**
 * Data for the extra health perk
 */
public class ExtraHealthData extends PerkData<ExtraHealthLevel> {

    @Override
    public @NotNull String getEquipmentType() {
        return EquipmentType.EXTRA_HEALTH.name();
    }

}
