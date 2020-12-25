package io.github.zap.zombies.game.data.level;

import lombok.Getter;

/**
 * A level of a gun that contains all of its numerical statistics
 */
public class GunLevel  {

    @Getter
    private float damage;

    @Getter
    private int ammo;

    @Getter
    private int clipAmmo;

    @Getter
    private float fireRate;

    @Getter
    private float reloadRate;

    public GunLevel(float damage, int ammo, int clipAmmo, float fireRate, float reloadRate) {
        this.damage = damage;
        this.ammo = ammo;
        this.clipAmmo = clipAmmo;
        this.fireRate = fireRate;
        this.reloadRate = reloadRate;
    }

    private GunLevel() {

    }

}
