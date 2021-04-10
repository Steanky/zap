package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.equipment.EquipmentType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Data for the speed perk
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class SpeedPerkData extends PerkData<SpeedPerkLevel> {

    private int speedReapplyInterval = 500;

    @Override
    public @NotNull String getEquipmentType() {
        return EquipmentType.SPEED.name();
    }

}
