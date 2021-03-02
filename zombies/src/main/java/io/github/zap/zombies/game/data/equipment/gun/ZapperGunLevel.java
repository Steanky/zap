package io.github.zap.zombies.game.data.equipment.gun;

import lombok.Getter;

/**
 * Level of a zapper gun
 */
@Getter
public class ZapperGunLevel extends LinearGunLevel {

    private int maxChainedEntities;

    private double maxChainDistance;

    public ZapperGunLevel(int range, float damage, double knockbackFactor, Integer freezeTime, int ammo, int clipAmmo, int fireRate, int reloadRate, int goldPerShot, int goldPerHeadshot, int maxPierceableEntities, int maxChainedEntities, int maxChainDistance) {
        super(range, damage, knockbackFactor, freezeTime, ammo, clipAmmo, fireRate, reloadRate, goldPerShot, goldPerHeadshot, maxPierceableEntities);
        this.maxChainedEntities = maxChainedEntities;
        this.maxChainDistance = maxChainDistance;
    }

    public ZapperGunLevel() {
        
    }

}
