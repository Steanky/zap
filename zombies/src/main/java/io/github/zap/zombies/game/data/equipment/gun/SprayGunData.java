package io.github.zap.zombies.game.data.equipment.gun;

import io.github.zap.zombies.game.equipment.EquipmentType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Data for a spray gun
 */
@Getter
public class SprayGunData extends ParticleGunData<SprayGunLevel> {

    @Override
    public @NotNull String getEquipmentType() {
        return EquipmentType.SPRAY_GUN.name();
    }

}
