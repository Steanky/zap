package io.github.zap.zombies.game.equipment.gun.logic;

import io.github.zap.zombies.game.DamageAttempt;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.gun.ZapperGunLevel;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.util.ParticleDataWrapper;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.*;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Beam that "zaps" nearby entities when it hits a target entity
 */
public class ZapperBeam extends LinearBeam {
    @RequiredArgsConstructor
    private class ZapperAoeDamageAttempt implements DamageAttempt {
        private final Vector directionVector;

        @Override
        public int getCoins() {
            return getGoldPerShot();
        }

        @Override
        public double damageAmount() {
            return getDamage() * aoeDamageFactor;
        }

        @Override
        public boolean ignoresArmor() {
            return false;
        }

        @Override
        public @NotNull Vector directionVector() {
            return directionVector;
        }

        @Override
        public double knockbackFactor() {
            return getKnockbackFactor();
        }
    }

    private final Set<Mob> hitMobs = new HashSet<>();

    private final int maxChainedEntities;

    private final double maxChainDistance;

    private final double aoeDamageFactor;

    public ZapperBeam(MapData mapData, ZombiesPlayer zombiesPlayer, Location root, ZapperGunLevel zapperGunLevel,
                      Particle particle, ParticleDataWrapper<?> particleDataWrapper, int particleCount) {
        super(mapData, zombiesPlayer, root, zapperGunLevel, particle, particleDataWrapper, particleCount);

        this.maxChainedEntities = zapperGunLevel.getMaxChainedEntities();
        this.maxChainDistance = zapperGunLevel.getMaxChainDistance();
        this.aoeDamageFactor = zapperGunLevel.getAoeHitDamageFactor();
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
                    ZombiesArena arena = getZombiesPlayer().getArena();
                    arena.getDamageHandler().damageEntity(getZombiesPlayer(),
                            new ZapperAoeDamageAttempt(mobToZap.getLocation().subtract(mob.getLocation())
                                    .toVector().normalize().multiply(getKnockbackFactor())), mobToZap);

                    attackedMobs.add(mobToZap);
                    counter++;
                }
            }
        }
    }

    @Override
    protected void damageEntity(RayTraceResult rayTraceResult) {
        super.damageEntity(rayTraceResult);

        Mob mob = (Mob)rayTraceResult.getHitEntity();

        //i think this should work; the mob is definitely damaged if != null
        if (mob != null) {
            hitMobs.add(mob);
        }
    }
}
