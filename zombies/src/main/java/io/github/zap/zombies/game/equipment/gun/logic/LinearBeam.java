package io.github.zap.zombies.game.equipment.gun.logic;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Sends lines of particles from guns
 */
@RequiredArgsConstructor
public class LinearBeam {

    private final static int PARTICLE_NUMBER = 4; // TODO: check

    private final double VELOCITY_FACTOR = 0.5;

    private final Set<Mob> hitMobs = new HashSet<>();

    private final World world;
    private final Location root;
    private final Vector directionVector;
    private final double distance;
    private final Particle particle;
    private final int maxHitEntities;
    private final double damage;

    /**
     * Sends the bullet
     */
    public void send() {
        hitScan();
        spawnParticles();
    }

    /**
     * Performs a hitscan calculations on the entities to target
     */
    private void hitScan() {
        while (hitMobs.size() != maxHitEntities) {
            RayTraceResult rayTraceResult = world.rayTraceEntities(
                    root,
                    directionVector,
                    distance,
                    (Entity entity) -> (entity instanceof Mob && !hitMobs.contains(entity))
            );

            if (rayTraceResult == null) {
                break;
            } else {
                damageEntity(rayTraceResult);
            }
        }
    }

    /**
     * Damages an entity from a ray trace
     * @param rayTraceResult The ray trace result to get the entity from
     */
    private void damageEntity(RayTraceResult rayTraceResult) {
        Mob mob = (Mob) rayTraceResult.getHitEntity();

        if (mob != null) {
            if (determineIfHeadshot(rayTraceResult, mob)) {
                mob.setHealth(mob.getHealth() - damage);
            } else {
                mob.damage(damage);
            }
            mob.setVelocity(mob.getVelocity().add(directionVector.clone().multiply(VELOCITY_FACTOR)));

            hitMobs.add(mob);
        }
    }

    /**
     * Determines whether or not a bullet was a headshot
     * @param rayTraceResult The ray trace of the bullet
     * @param mob The targeted mob
     * @return Whether or not the shot was a headshot
     */
    private boolean determineIfHeadshot(RayTraceResult rayTraceResult, Mob mob) {
        double mobY = mob.getLocation().getY();
        double eyeY = mobY + mob.getEyeHeight();
        double heightY = mobY + mob.getHeight();

        Vector hitPosition = rayTraceResult.getHitPosition();
        double yPos = hitPosition.getY();

        return  (2 * eyeY - heightY <= yPos && yPos <= heightY);
    }

    /**
     * Spawns the bullet's particles in a line
     */
    private void spawnParticles() {
        for (int i = 0; i < PARTICLE_NUMBER; i++) {
            world.spawnParticle(
                    particle,
                    root,
                    0,
                    0,
                    0,
                    0
            );
            root.add(directionVector);
        }
    }

}
