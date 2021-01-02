package io.github.zap.zombies.game.equipment.gun.logic;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Predicate;

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
        for (ImmutablePair<RayTraceResult, Double> hit : rayTrace()) {
            damageEntity(hit.getLeft());
        }
    }

    /**
     * Gets all the entities hit by the beam's ray trace
     * Uses modified ray trace from
     * {@link org.bukkit.craftbukkit.v1_16_R3.CraftWorld#rayTraceEntities(Location, Vector, double, double, Predicate)}
     * @return The entities that should be hit by the bullet
     */
    private Iterable<ImmutablePair<RayTraceResult, Double>> rayTrace() {
        if (maxHitEntities == 0) {
            return Collections.emptySet();
        } else {
            Queue<ImmutablePair<RayTraceResult, Double>> queue = new PriorityQueue<>(
                    maxHitEntities,
                    Comparator.comparingDouble(ImmutablePair::getRight)
            );

            Vector startPos = root.toVector();
            Vector dir = directionVector.clone().normalize().multiply(distance);

            BoundingBox aabb = BoundingBox.of(startPos, startPos).expandDirectional(dir);
            Collection<Entity> entities = root.getWorld().getNearbyEntities(
                    aabb,
                    (Entity entity) -> entity instanceof Mob
            );

            Iterator<Entity> iterator = entities.iterator();

            // Fill up queue until max entities reach
            while (iterator.hasNext() && queue.size() < maxHitEntities) {
                Entity entity = iterator.next();
                BoundingBox boundingBox = entity.getBoundingBox();
                RayTraceResult hitResult = boundingBox.rayTrace(startPos, directionVector, distance);

                if (hitResult != null) {
                    double distanceSq = startPos.distanceSquared(hitResult.getHitPosition());
                    queue.add(
                            ImmutablePair.of(
                                    new RayTraceResult(hitResult.getHitPosition(), entity, hitResult.getHitBlockFace()),
                                    distanceSq
                            )
                    );
                }
            }

            // Perform distance checks on proceeding entities
            double maxDist = (queue.size() > 0) ? queue.peek().getRight() : 1.7976931348623157E308D;
            while (iterator.hasNext()) {
                Entity entity = iterator.next();
                BoundingBox boundingBox = entity.getBoundingBox().expand(0.0D);
                RayTraceResult hitResult = boundingBox.rayTrace(startPos, directionVector, distance);

                if (hitResult != null) {
                    double distanceSq = startPos.distanceSquared(hitResult.getHitPosition());
                    if (distanceSq < maxDist) {
                        queue.poll();
                        queue.add(
                                ImmutablePair.of(
                                        new RayTraceResult(
                                                hitResult.getHitPosition(),
                                                entity, hitResult.getHitBlockFace()
                                        ),
                                        distanceSq
                                )
                        );

                        ImmutablePair<RayTraceResult, Double> pair = queue.peek();
                        assert pair != null;
                        maxDist = pair.getRight();
                    }
                }
            }

            return queue;
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
