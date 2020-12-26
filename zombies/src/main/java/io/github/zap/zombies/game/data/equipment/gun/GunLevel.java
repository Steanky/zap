package io.github.zap.zombies.game.data.equipment.gun;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A level of a gun that contains all of its numerical statistics
 */
@AllArgsConstructor
@Getter
public class GunLevel  {

    private float damage;

    private int ammo;

    private int clipAmmo;

    private float fireRate;

    private float reloadRate;

    protected GunLevel() {

    }

}
