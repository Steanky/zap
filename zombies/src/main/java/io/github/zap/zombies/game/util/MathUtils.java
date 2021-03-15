package io.github.zap.zombies.game.util;

import io.github.zap.arenaapi.particle.RectangularPrism;
import io.github.zap.zombies.Zombies;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Predicate;

public class MathUtils {
    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public static double normalizeMultiplier(double value, double upperValue) {
        if(value > 0) {
            return clamp(value, 0, upperValue);
        } else {
            return upperValue - clamp(-value, 0, upperValue);
        }
    }

    public static List<RayTraceResult> sortedRayTraceEntities(Location origin, Vector direction, double maxDistance,
                                                              int entityCap, Predicate<Entity> filter) {
        List<RayTraceResult> results = new ArrayList<>();
        Vector originVector = origin.toVector();
        rayTraceInternal(-1, results, (List<Entity>)origin.getWorld()
                .getNearbyEntities(BoundingBox.of(originVector, originVector).expandDirectional(direction.clone()
                        .normalize().multiply(maxDistance)), filter), filter, originVector, direction, maxDistance,
                entityCap);
        return results;
    }

    private static Pair<Boolean, Integer> rayTraceInternal(int excludeIndex, List<RayTraceResult> results,
                                                           List<Entity> sampleEntities, Predicate<Entity> filter,
                                                           Vector origin, Vector direction, double rayLength, int entityCap) {
        if(results.size() == entityCap || sampleEntities.size() == 0) {
            return Pair.of(true, 0);
        }

        int removedBeforeExclusion = 0;
        for(int i = sampleEntities.size() - 1; i > -1; i--) { //iterate through unsorted sample entities
            if(i == excludeIndex) {
                continue;
            }

            Entity sampleEntity = sampleEntities.get(i);

            if(filter.test(sampleEntity)) {
                BoundingBox entityBounds = sampleEntity.getBoundingBox();
                RayTraceResult test = entityBounds.rayTrace(origin, direction, rayLength);

                if(test != null) { //we have a hit
                    test = new RayTraceResult(test.getHitPosition(), sampleEntity, test.getHitBlockFace());

                    Zombies.info(excludeIndex + " Sample entity before shift: " + sampleEntity.getUniqueId());

                    //check for closer entities. might be a better way to get the ray length rather than a call to distance()
                    Pair<Boolean, Integer> next = rayTraceInternal(i, results, sampleEntities,
                            filter, origin, direction, origin.distance(test.getHitPosition()), entityCap);

                    if(results.size() == entityCap || sampleEntities.size() == 0) {
                        return Pair.of(true, 0);
                    }

                    //offset by any entities that were removed before i
                    int removedByNext = next.getRight();

                    removedBeforeExclusion += removedByNext;
                    i -= removedByNext;

                    Zombies.info(excludeIndex + " Sample entity after shifting: " + sampleEntities.get(i).getUniqueId());

                    //if there are no closer entities, add to results (up to entityCap)
                    if(!next.getLeft()) {
                        sampleEntities.remove(i);

                        if(i <= excludeIndex) {
                            removedBeforeExclusion++;
                        }

                        if(results.size() < entityCap) {
                            results.add(test);
                        }
                    }
                }
                else if(BoundingBox.of(origin, direction.clone().multiply(rayLength)).contains(entityBounds)) {
                    /*
                    we can remove the entity if it is fully inside the current bounds and yet still did not pass the
                    raytrace; it is guaranteed to never be needed again
                     */
                    sampleEntities.remove(i);

                    if(i <= excludeIndex) {
                        removedBeforeExclusion++;
                    }
                }
            }
            else {
                //if the predicate failed, remove the entity in all cases
                sampleEntities.remove(i);

                if(i <= excludeIndex) {
                    removedBeforeExclusion++;
                }
            }
        }

        return Pair.of(false, removedBeforeExclusion);
    }
}
