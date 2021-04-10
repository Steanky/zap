package io.github.zap.zombies.game.data.equipment.gun;

import io.github.zap.zombies.game.equipment.EquipmentType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Data for a linear gun
 */
@Getter
public class LinearGunData extends ParticleGunData<LinearGunLevel> {

    @Override
    public @NotNull String getEquipmentType() {
        return EquipmentType.LINEAR_GUN.name();
    }

}
