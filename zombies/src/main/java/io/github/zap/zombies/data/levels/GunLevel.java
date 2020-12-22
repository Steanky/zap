package io.github.zap.zombies.data.levels;

import lombok.Getter;

/**
 * A level of a gun that contains all of its numerical statistics
 */
public class GunLevel extends CostLevel {

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

    public GunLevel(int cost, int damage, int ammo, int clipAmmo, int fireRate, int reloadRate) {
        super(cost);

        this.damage = damage;
        this.ammo = ammo;
        this.clipAmmo = clipAmmo;
        this.fireRate = fireRate;
        this.reloadRate = reloadRate;
    }

}
