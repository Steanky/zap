package io.github.zap.zombies.game.equipment.gun.logic;

import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.gun.ZapperGunLevel;
import io.github.zap.zombies.game.data.map.MapData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Beam that "zaps" nearby entities when it hits a target entity
 */
public class ZapperBeam extends LinearBeam {

    private final Set<Mob> hitMobs = new HashSet<>();

    private final int maxChainedEntities;

    private final double maxChainDistance;

    public ZapperBeam(MapData mapData, ZombiesPlayer zombiesPlayer,
                      Location root, Particle particle, ZapperGunLevel zapperGunLevel, int particleCount) {
        super(mapData, zombiesPlayer, root, particle, zapperGunLevel, particleCount);

        this.maxChainedEntities = zapperGunLevel.getMaxChainedEntities();
        this.maxChainDistance = zapperGunLevel.getMaxChainDistance();
    }

    public ZapperBeam(MapData mapData, ZombiesPlayer zombiesPlayer, Location root, Particle particle,
                      ZapperGunLevel zapperGunLevel) {
        this(mapData, zombiesPlayer, root, particle, zapperGunLevel, DEFAULT_PARTICLE_COUNT);
    }

    @Override
    protected void hitScan() {
        super.hitScan();

        World world = getWorld();
        Set<Mob> attackedMobs = new HashSet<>(hitMobs);
        for (Mob mob : hitMobs) {
            Iterator<Mob> mobsToZap = world.getNearbyEntitiesByType(Mob.class, mob.getLocation(), maxChainDistance)
                    .iterator();
            int counter = 0;

            while (mobsToZap.hasNext() && counter < maxChainedEntities) {
                Mob mobToZap = mobsToZap.next();

                if (!attackedMobs.contains(mobToZap) && !hitMobs.contains(mobToZap)) {
                    mobToZap.damage(getDamage());

                    Vector knockbackVector = mob.getLocation().subtract(mobToZap.getLocation()).toVector().normalize()
                            .multiply(getKnockbackFactor());
                    mobToZap.setVelocity(mobToZap.getVelocity().add(knockbackVector));

                    attackedMobs.add(mobToZap);
                    counter++;
                }
            }
        }
    }

    @Override
    protected void damageEntity(RayTraceResult rayTraceResult) {
        Mob mob = (Mob) rayTraceResult.getHitEntity();

        if (mob != null) {
            if (determineIfHeadshot(rayTraceResult, mob)) {
                mob.setHealth(mob.getHealth() - getDamage());
                getZombiesPlayer().addCoins(getGoldPerHeadshot());
            } else {
                mob.damage(getDamage());
                getZombiesPlayer().addCoins(getGoldPerShot());
            }

            mob.setVelocity(mob.getVelocity().add(getDirectionVector().clone().multiply(getKnockbackFactor())));

            hitMobs.add(mob);

            if (mob.getHealth() <= 0) {
                getZombiesPlayer().incrementKills();
            }
        }
    }
}
