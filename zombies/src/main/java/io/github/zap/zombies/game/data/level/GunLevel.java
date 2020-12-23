package io.github.zap.zombies.game.data.level;

import lombok.Getter;

/**
 * A level of a gun that contains all of its numerical statistics
 */
public class GunLevel  {

    @Getter
    private final int damage;

    @Getter
    private final int ammo;

    @Getter
    private final int clipAmmo;

    @Getter
    private final float fireRate;

    @Getter
    private final float reloadRate;

    public GunLevel(int damage, int ammo, int clipAmmo, int fireRate, int reloadRate) {
        this.damage = damage;
        this.ammo = ammo;
        this.clipAmmo = clipAmmo;
        this.fireRate = fireRate;
        this.reloadRate = reloadRate;
    }

}
