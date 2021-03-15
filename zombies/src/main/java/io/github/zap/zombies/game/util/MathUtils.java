package io.github.zap.zombies.game.util;

import io.github.zap.zombies.Zombies;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
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
        rayTraceInternal(new HashSet<>(), -1, results, (List<Entity>)origin.getWorld()
                .getNearbyEntities(BoundingBox.of(originVector, originVector).expandDirectional(direction.clone()
                        .normalize().multiply(maxDistance)), filter), filter, originVector, direction, maxDistance,
                entityCap);

        return results;
    }

    private static Pair<Boolean, Integer> rayTraceInternal(Set<UUID> blacklist, int lastIndex, List<RayTraceResult> results,
                                                           List<Entity> sampleEntities, Predicate<Entity> filter,
                                                           Vector origin, Vector direction, double rayLength, int entityCap) {
        int removedBeforeLast = 0;
        for(int i = sampleEntities.size() - 1; i > -1; i--) { //iterate through unsorted sample entities
            Entity sampleEntity = sampleEntities.get(i);

            if(blacklist.contains(sampleEntity.getUniqueId())) {
                continue;
            }

            if(filter.test(sampleEntity)) {
                BoundingBox entityBounds = sampleEntity.getBoundingBox();
                RayTraceResult test = entityBounds.rayTrace(origin, direction, rayLength);

                if(test != null) { //we have a hit
                    test = new RayTraceResult(test.getHitPosition(), sampleEntity, test.getHitBlockFace());

                    blacklist.add(sampleEntity.getUniqueId());

                    //check for closer entities. might be a better way to get the ray length rather than a call to distance()
                    Pair<Boolean, Integer> next = rayTraceInternal(blacklist, i, results, sampleEntities,
                            filter, origin, direction, origin.distance(test.getHitPosition()), entityCap);

                    if(results.size() == entityCap || sampleEntities.size() == 0) {
                        return Pair.of(false, 0);
                    }

                    blacklist.remove(sampleEntity.getUniqueId());

                    //offset by any entities that were removed before i
                    int removedByNext = next.getRight();

                    removedBeforeLast += removedByNext;
                    i -= removedByNext;

                    //if there are no closer entities, add to results (up to entityCap)
                    if(!next.getLeft()) {
                        if(i > -1 && i < sampleEntities.size()) {
                            sampleEntities.remove(i);

                            if(i < lastIndex) {
                                removedBeforeLast++;
                            }
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

                    if(i < lastIndex) {
                        removedBeforeLast++;
                    }
                }
            }
            else {
                //if the predicate failed, remove the entity in all cases
                sampleEntities.remove(i);

                if(i < lastIndex) {
                    removedBeforeLast++;
                }
            }
        }

        return Pair.of(false, removedBeforeLast);
    }

    private static  List<RayTraceResult> rayTraceInternalIterative(List<Entity> sampleEntities, Predicate<Entity> filter,
                                                           Vector origin, Vector direction, double rayLength, int entityCap) {
        Deque<RayTraceResult> deque = new ArrayDeque<>();
        double bestDistance = Double.MAX_VALUE;
        int anchor = -1;

        for(int i = sampleEntities.size() - 1; i > -1; i--) {
            Entity sampleEntity = sampleEntities.get(i);

            if(filter.test(sampleEntity)) {
                BoundingBox bounds = sampleEntity.getBoundingBox();
                RayTraceResult rayTrace = bounds.rayTrace(origin, direction, rayLength);

                if(rayTrace != null) {
                    if(anchor == -1 || origin.distance(sampleEntities.get(i).getLocation().toVector()) < bestDistance) {
                        anchor = i;
                    }
                }
                else if(BoundingBox.of(origin, direction.clone().multiply(rayLength)).contains(bounds)) {
                    sampleEntities.remove(i);
                }
            }
            else {
                sampleEntities.remove(i);
            }
        }

        return new ArrayList<>();
    }
}
