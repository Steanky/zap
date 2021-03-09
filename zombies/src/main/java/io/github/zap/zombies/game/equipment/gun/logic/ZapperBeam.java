package io.github.zap.zombies.game.equipment.gun.logic;

import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.gun.ZapperGunLevel;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.util.ParticleDataWrapper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.*;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
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

    public ZapperBeam(MapData mapData, ZombiesPlayer zombiesPlayer, Location root, ZapperGunLevel zapperGunLevel,
                      Particle particle, ParticleDataWrapper<?> particleDataWrapper, int particleCount) {
        super(mapData, zombiesPlayer, root, zapperGunLevel, particle, particleDataWrapper, particleCount);

        this.maxChainedEntities = zapperGunLevel.getMaxChainedEntities();
        this.maxChainDistance = zapperGunLevel.getMaxChainDistance();
    }

    public ZapperBeam(MapData mapData, ZombiesPlayer zombiesPlayer, Location root, ZapperGunLevel zapperGunLevel,
                      Particle particle, ParticleDataWrapper<?> particleDataWrapper) {
        this(mapData, zombiesPlayer, root, zapperGunLevel, particle, particleDataWrapper, DEFAULT_PARTICLE_COUNT);
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

                    Vector knockbackVector = mobToZap.getLocation().subtract(mob.getLocation()).toVector().normalize()
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

        if (mob != null && getBukkitAPIHelper().isMythicMob(mob)) {
            ZombiesPlayer zombiesPlayer = getZombiesPlayer();
            Player player = zombiesPlayer.getPlayer();

            boolean isCritical = determineIfHeadshot(rayTraceResult, mob);
            mob.playEffect(EntityEffect.HURT);
            inflictDamage(mob, getDamage(), isCritical);
            zombiesPlayer.addCoins(isCritical ? getGoldPerHeadshot() : getGoldPerShot());
            player.playSound(Sound.sound(
                    Key.key("minecraft:entity.arrow.hit_player"),
                    Sound.Source.MASTER,
                    1.0F,
                    isCritical ? 2.0F : 1.5F
            ));
            mob.setVelocity(mob.getVelocity().add(getDirectionVector().clone().multiply(getKnockbackFactor())));

            hitMobs.add(mob);

            if (mob.getHealth() <= 0) {
                getZombiesPlayer().incrementKills();
            }
        }
    }
}
