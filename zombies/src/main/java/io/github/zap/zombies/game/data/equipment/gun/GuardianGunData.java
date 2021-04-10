package io.github.zap.zombies.game.data.equipment.gun;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents data for a guardian gun
 */
public class GuardianGunData extends GunData<GuardianGunLevel> {

    @Override
    public @NotNull String getEquipmentType() {
        return EquipmentType.GUARDIAN.name();
    }

}
