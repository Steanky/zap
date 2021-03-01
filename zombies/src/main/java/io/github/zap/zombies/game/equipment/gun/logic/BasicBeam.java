package io.github.zap.zombies.game.equipment.gun.logic;

import com.google.common.collect.Sets;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunLevel;
import io.github.zap.zombies.game.data.map.MapData;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Predicate;

/**
 * Represents a beam used by guns
 */
@Getter
public class BasicBeam {
    private final static Set<Material> AIR_MATERIALS =
            Sets.newHashSet(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);

    private final MapData mapData;
    private final ZombiesPlayer zombiesPlayer;
    private final World world;
    private final Vector root;
    private final Vector directionVector;
    private final double distance;
    private final int maxPierceableEntities;
    private final int range;
    private final double damage;
    private final double knockbackFactor;
    private final int goldPerShot;
    private final int goldPerHeadshot;


    public BasicBeam(MapData mapData, ZombiesPlayer zombiesPlayer, Location root, LinearGunLevel level) {
        this.mapData = mapData;
        this.zombiesPlayer = zombiesPlayer;
        this.world = root.getWorld();
        this.root = root.toVector();
        this.directionVector = root.getDirection();
        this.distance = getDistance();


        this.maxPierceableEntities = level.getMaxPierceableEntities();
        this.range = Math.min(120, level.getRange());
        this.damage = level.getDamage();
        this.knockbackFactor = level.getKnockbackFactor();
        this.goldPerShot = level.getGoldPerShot();
        this.goldPerHeadshot = level.getGoldPerHeadshot();
    }

    /**
     * Gets the distance to the shot's target block
     * @return The distance to the shot's target block
     */
    private double getDistance() {
        Block targetBlock = getTargetBlock();
        BoundingBox boundingBox;

        if (AIR_MATERIALS.contains(targetBlock.getType())) {
            Location location = targetBlock.getLocation();
            boundingBox = new BoundingBox(location.getX(), targetBlock.getY(), targetBlock.getZ(),
                    location.getX() + 1, location.getY() + 1, targetBlock.getZ() + 1);
        } else {
            boundingBox = targetBlock.getBoundingBox();
        }

        return Objects.requireNonNull(
                boundingBox.rayTrace(
                        root,
                        directionVector,
                        range + 1.74) // sqrt(3) error to account for entire block
        ).getHitPosition().distance(root);
    }

    /**
     * Gets the targeted block of the shot
     * Adapted from {@link org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity#getTargetBlock(int)} nested calls
     * @return The targeted block
     */
    private Block getTargetBlock() {
        Block targetBlock = null;
        Iterator<Block> iterator = new BlockIterator(world, root, directionVector, 0.0D, range);

        while (iterator.hasNext()) {
            targetBlock = iterator.next();

            Material material = targetBlock.getType();
            if (!AIR_MATERIALS.contains(material) && mapData.windowAt(targetBlock.getLocation().toVector()) != null) {
                break;
            }
        }

        return targetBlock;
    }

    /**
     * Sends the bullet
     */
    public void send() {
        hitScan();
    }

    /**
     * Performs a hitscan calculations on the entities to target
     */
    protected void hitScan() {
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
        if (maxPierceableEntities == 0) {
            return Collections.emptySet();
        } else {
            Queue<ImmutablePair<RayTraceResult, Double>> queue = new PriorityQueue<>(
                    maxPierceableEntities,
                    Comparator.comparingDouble(ImmutablePair::getRight)
            );

            Iterator<Entity> iterator = getEntitiesInPathIterator();

            fillQueue(queue, iterator);
            replaceFarthestEnqueuedEntities(queue, iterator);

            return queue;
        }
    }

    /**
     * Gets an iterator of all the entities within the bullet's path
     * @return The iterator
     */
    private Iterator<Entity> getEntitiesInPathIterator() {
        Vector dir = directionVector.clone().normalize().multiply(distance);

        BoundingBox aabb = BoundingBox.of(root, root).expandDirectional(dir);
        return world.getNearbyEntities(
                aabb,
                (Entity entity) -> entity instanceof Mob
        ).iterator();
    }

    /**
     * Fills the queue up with entities until it has reached the maxmimum hit entities
     * @param queue The queue to fill up
     * @param iterator The entity iterable iterator
     */
    private void fillQueue(Queue<ImmutablePair<RayTraceResult, Double>> queue, Iterator<Entity> iterator) {
        while (iterator.hasNext() && queue.size() < maxPierceableEntities) {
            Entity entity = iterator.next();
            BoundingBox boundingBox = entity.getBoundingBox();
            RayTraceResult hitResult = boundingBox.rayTrace(root, directionVector, distance);

            if (hitResult != null) {
                double distanceSq = root.distanceSquared(hitResult.getHitPosition());
                queue.add(
                        ImmutablePair.of(
                                new RayTraceResult(hitResult.getHitPosition(), entity, hitResult.getHitBlockFace()),
                                distanceSq
                        )
                );
            }
        }
    }

    /**
     * Replaces the farthest entities within the queue so that only the closest entities are shot
     * @param queue The queue to replace entities within
     * @param iterator The entity iterable iterator
     */
    private void replaceFarthestEnqueuedEntities(Queue<ImmutablePair<RayTraceResult, Double>> queue,
                                                 Iterator<Entity> iterator) {
        double maxDist = (queue.size() > 0) ? queue.peek().getRight() : Double.POSITIVE_INFINITY;

        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            BoundingBox boundingBox = entity.getBoundingBox();
            RayTraceResult hitResult = boundingBox.rayTrace(root, directionVector, distance);

            if (hitResult != null) {
                double distanceSq = root.distanceSquared(hitResult.getHitPosition());
                if (distanceSq < maxDist) {
                    queue.poll(); // Remove entity farthest away in queue
                    queue.add(
                            ImmutablePair.of(
                                    new RayTraceResult(
                                            hitResult.getHitPosition(),
                                            entity, hitResult.getHitBlockFace()
                                    ),
                                    distanceSq
                            )
                    );

                    // Get the distance of the farthest mob so we don't have to check again
                    ImmutablePair<RayTraceResult, Double> pair = queue.peek();
                    assert pair != null;
                    maxDist = pair.getRight();
                }
            }
        }
    }

    /**
     * Damages an entity from a ray trace
     * @param rayTraceResult The ray trace result to get the entity from
     */
    protected void damageEntity(RayTraceResult rayTraceResult) {
        Mob mob = (Mob) rayTraceResult.getHitEntity();

        if (mob != null) {
            if (determineIfHeadshot(rayTraceResult, mob)) {
                mob.setHealth(mob.getHealth() - damage);
                zombiesPlayer.addCoins(goldPerHeadshot);
            } else {
                mob.damage(damage);
                zombiesPlayer.addCoins(goldPerShot);
            }

            mob.setVelocity(mob.getVelocity().add(directionVector.clone().multiply(knockbackFactor)));

            if (mob.getHealth() <= 0) {
                zombiesPlayer.incrementKills();
            }
        }
    }

    /**
     * Determines whether or not a bullet was a headshot
     * @param rayTraceResult The ray trace of the bullet
     * @param mob The targeted mob
     * @return Whether or not the shot was a headshot
     */
    protected boolean determineIfHeadshot(RayTraceResult rayTraceResult, Mob mob) {
        double mobY = mob.getLocation().getY();
        double eyeY = mobY + mob.getEyeHeight();
        double heightY = mobY + mob.getHeight();

        Vector hitPosition = rayTraceResult.getHitPosition();
        double yPos = hitPosition.getY();

        return (2 * eyeY - heightY <= yPos && yPos <= heightY);
    }
}
