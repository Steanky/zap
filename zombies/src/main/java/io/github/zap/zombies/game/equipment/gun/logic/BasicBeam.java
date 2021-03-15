package io.github.zap.zombies.game.equipment.gun.logic;

import io.github.zap.zombies.game.DamageAttempt;
import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunLevel;
import io.github.zap.zombies.game.data.map.MapData;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.BukkitAPIHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

/**
 * Represents a beam used by guns
 */
@Getter
public class BasicBeam {

    @RequiredArgsConstructor
    private class BeamDamageAttempt implements DamageAttempt {
        private final boolean isHeadshot;

        @Override
        public int getCoins(@NotNull Damager damager, @NotNull Mob target) {
            return isHeadshot ? goldPerHeadshot : goldPerShot;
        }

        @Override
        public double damageAmount(@NotNull Damager damager, @NotNull Mob target) {
            return damage;
        }

        @Override
        public boolean ignoresArmor(@NotNull Damager damager, @NotNull Mob target) {
            return isHeadshot;
        }

        @Override
        public @NotNull Vector directionVector(@NotNull Damager damager, @NotNull Mob target) {
            return directionVector;
        }

        @Override
        public double knockbackFactor(@NotNull Damager damager, @NotNull Mob target) {
            return knockbackFactor;
        }
    }

    private final BukkitAPIHelper bukkitAPIHelper;

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
        this.bukkitAPIHelper = MythicMobs.inst().getAPIHelper();

        this.mapData = mapData;
        this.zombiesPlayer = zombiesPlayer;
        this.world = root.getWorld();
        this.root = root.toVector();
        this.directionVector = root.getDirection().clone();

        this.maxPierceableEntities = level.getMaxPierceableEntities();
        this.range = Math.min(120, level.getRange());
        this.damage = level.getDamage();
        this.knockbackFactor = level.getKnockbackFactor();
        this.goldPerShot = level.getGoldPerShot();
        this.goldPerHeadshot = level.getGoldPerHeadshot();

        this.distance = getDistance();
    }

    /**
     * Gets the distance to the shot's target block
     * @return The distance to the shot's target block
     */
    private double getDistance() {
        Block targetBlock = getTargetBlock();
        BoundingBox boundingBox;

        if (targetBlock.getType().isAir()) {
            Location location = targetBlock.getLocation();
            boundingBox = new BoundingBox(location.getX(), location.getY(), location.getZ(),
                    location.getX() + 1, location.getY() + 1, location.getZ() + 1);
        } else {
            boundingBox = targetBlock.getBoundingBox();
        }

        RayTraceResult rayTraceResult = boundingBox.rayTrace(root, directionVector,range + 1.74);
        if (rayTraceResult != null) {
            return rayTraceResult.getHitPosition().distance(root);
        } else {
            return range;
        }
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

            if (!targetBlock.getType().isAir() && mapData.windowAt(targetBlock.getLocation().toVector()) == null) {
                BoundingBox boundingBox = targetBlock.getBoundingBox();
                if (boundingBox.getWidthX() != 1.0D
                        || boundingBox.getHeight() != 1.0D || boundingBox.getWidthZ() != 1.0D) {
                    RayTraceResult rayTraceResult = boundingBox.rayTrace(root, directionVector,range + 1.74);

                    if (rayTraceResult != null) {
                        break;
                    }
                } else {
                    break;
                }
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
        for (RayTraceResult rayTraceResult : rayTrace()) {
            damageEntity(rayTraceResult);
        }
    }

    /**
     * Gets all the ray trace results hit by the bullet's ray trace
     * @return The ray traces of the entities that should be hit by the bullet
     */
    private List<RayTraceResult> rayTrace() {
        if (maxPierceableEntities == 0) {
            return Collections.emptyList();
        } else {
            List<Entity> entities = new ArrayList<>(getNearbyEntities());

            Map<Entity, Double> distances = new HashMap<>();
            Function<Entity, Double> distanceComp
                    = entity -> this.root.distanceSquared(entity.getLocation().toVector());
            entities.sort((o1, o2) -> {
                double d1 = distances.computeIfAbsent(o1, distanceComp);
                double d2 = distances.computeIfAbsent(o2, distanceComp);

                return Double.compare(d1, d2);
            });

            List<RayTraceResult> rayTraceResults = new ArrayList<>(maxPierceableEntities);

            for (Entity entity : entities) {
                BoundingBox entityBoundingBox = entity.getBoundingBox();
                RayTraceResult hitResult = entityBoundingBox.rayTrace(root, directionVector, distance);

                if (hitResult != null) {
                    rayTraceResults.add(
                            new RayTraceResult(hitResult.getHitPosition(), entity, hitResult.getHitBlockFace())
                    );
                }
                if (rayTraceResults.size() == maxPierceableEntities) {
                    break;
                }
            }

            return rayTraceResults;
        }
    }

    /**
     * Gets an collection of all the entities near the bullet's path
     * @return The bullet
     */
    private Collection<Entity> getNearbyEntities() {
        Vector dir = directionVector.clone().normalize().multiply(distance);

        BoundingBox aabb = BoundingBox.of(root, root).expandDirectional(dir);
        return world.getNearbyEntities(
                aabb,
                (Entity entity) -> entity instanceof Mob
        );
    }

    /**
     * Damages an entity from a ray trace
     * @param rayTraceResult The ray trace result to get the entity from
     */
    protected void damageEntity(RayTraceResult rayTraceResult) {
        Mob mob = (Mob) rayTraceResult.getHitEntity();

        if (mob != null) {
            ZombiesArena arena = getZombiesPlayer().getArena();
            arena.getDamageHandler().damageEntity(getZombiesPlayer(),
                    new BeamDamageAttempt(determineIfHeadshot(rayTraceResult, mob)), mob);
        }
    }

    /**
     * Determines whether or not a bullet was a headshot
     * @param rayTraceResult The ray trace of the bullet
     * @param mob The targeted mob
     * @return Whether or not the shot was a headsh5ot
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
