package io.github.zap.zombies.game.data.equipment.gun;

import lombok.Getter;

public class LinearGunLevel extends GunLevel {

    @Getter
    private int maxPierceableEntities;

    public LinearGunLevel(float damage, int ammo, int clipAmmo, float fireRate, float reloadRate, int maxPierceableEntities) {
        super(damage, ammo, clipAmmo, fireRate, reloadRate);
        this.maxPierceableEntities = maxPierceableEntities;
    }

    private LinearGunLevel() {
        super();
    }

}
