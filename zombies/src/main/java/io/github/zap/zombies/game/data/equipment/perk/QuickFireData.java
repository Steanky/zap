package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.jetbrains.annotations.NotNull;

/**
 * Data for the quick fire perk
 */
public class QuickFireData extends PerkData<QuickFireLevel> {

    @Override
    public @NotNull String getEquipmentType() {
        return EquipmentType.QUICK_FIRE.name();
    }

}
