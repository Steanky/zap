package io.github.zap.zombies.game.data.equipment.gun;

import io.github.zap.zombies.game.equipment.EquipmentType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Data for a zapper gun
 */
@Getter
public class ZapperGunData extends ParticleGunData<ZapperGunLevel> {

    @Override
    public @NotNull String getEquipmentType() {
        return EquipmentType.ZAPPER.name();
    }

}
