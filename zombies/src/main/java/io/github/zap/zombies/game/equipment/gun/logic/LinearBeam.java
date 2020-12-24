package io.github.zap.zombies.game.equipment.gun.logic;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Sends lines of particles from guns
 */
public class LinearBeam {

    private final double distance;
    private final List<Entity> hitEntities = new ArrayList<>();

    private final World world;
    private final Vector particleVector;
    private final Vector directionVector;
    private final int maxHitEntities;
    private final Particle particle;

    public LinearBeam(World world, Particle particle, Vector eyeLocation, Vector directionVector, Vector targetBlockVector, int maxHitEntities) {
        this.distance = eyeLocation.distance(targetBlockVector);

        this.world = world;
        this.particle = particle;
        this.particleVector = eyeLocation.clone();
        this.directionVector = directionVector.clone();
        this.maxHitEntities = maxHitEntities;
    }

    /**
     * Sends the line of particles
     */
    public void send() {
        final int particleCount = (int) Math.floor(distance);

        for (int i = 0; i < particleCount; i++) {
            if (hitEntities.size() == maxHitEntities) {
                break;
            } else {
                world.spawnParticle(particle, particleVector.getX(), particleVector.getY(), particleVector.getZ(), 0, 0, 0, 0);
                findEntitiesInLineOfSight();
                particleVector.add(directionVector);
            }
        }

        for (Entity entity : hitEntities) {
            damageEntity(entity);
        }
    }

    /**
     * Gets all entities within the shooting player's line of sight
     */
    private void findEntitiesInLineOfSight() {
        for (Entity entity : world.getNearbyEntities(particleVector.toLocation(world), 1, 1 , 1)) {
            if (entity instanceof Mob) { //TODO: Change requirement
                final BoundingBox boundingBox = entity.getBoundingBox();

                if (boundingBox.rayTrace(particleVector, directionVector.clone().normalize(), 1) != null && hitEntities.size() < maxHitEntities) {
                    hitEntities.add(entity);
                    // TODO: Damaging the entities
                }
            }
        }

    }

    /**
     * Damages an actual entity
     * @param entity The entity to damage
     */
    private void damageEntity(Entity entity) {
        // TODO: Damaging the entities
    }
}
