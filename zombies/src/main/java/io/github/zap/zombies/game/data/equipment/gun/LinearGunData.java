package io.github.zap.zombies.game.data.equipment.gun;

import lombok.Getter;
import org.bukkit.Particle;

/**
 * Data for a linear gun
 */
@Getter
public class LinearGunData extends GunData<LinearGunLevel> {

    private Particle particle;

    private Object particleData;

    private LinearGunData() {

    }

}
