package io.github.zap.zombies.game.data.equipment.gun;

import lombok.Getter;

/**
 * Level of a spray gun
 */
@Getter
public class SprayGunLevel extends GunLevel {

    private float coneAngle;

    private int pellets;

    private int maxPierceableEntities;

    private SprayGunLevel() {

    }

}
