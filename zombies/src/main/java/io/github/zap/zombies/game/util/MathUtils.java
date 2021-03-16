package io.github.zap.zombies.game.util;

import lombok.Value;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        if(exclusionHash.size() > 0) {
            exclusionHash.clear();
        }

        rayTraceInternal(exclusionHash, -1, results, (List<Entity>)origin.getWorld()
                .getNearbyEntities(BoundingBox.of(originVector, originVector).expandDirectional(direction.clone()
                        .multiply(maxDistance)), filter), filter, originVector, direction, maxDistance,
                entityCap);

        return results;
    }

    private static final HashSet<UUID> exclusionHash = new HashSet<>();
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
                Vector endpoint = direction.clone().multiply(rayLength);
                BoundingBox entityBounds = sampleEntity.getBoundingBox();
                RayTraceResult test = fastRayTrace(entityBounds, origin, direction, endpoint);

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
                else if(BoundingBox.of(origin, endpoint).contains(entityBounds)) {
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

    private static ResultEntry fastRayTrace(Entity entity, Vector start, Vector direction, Vector hitPosition) {
        if(start.equals(hitPosition)) {
            return null;
        }
        else {
            BoundingBox test = entity.getBoundingBox();
            double startX = start.getX();
            double startY = start.getY();
            double startZ = start.getZ();
            double dirX = direction.getX();
            double dirY = direction.getY();
            double dirZ = direction.getZ();
            double divX = 1.0D / dirX;
            double divY = 1.0D / dirY;
            double divZ = 1.0D / dirZ;
            double tMin;
            double tMax;
            BlockFace hitBlockFaceMin;
            BlockFace hitBlockFaceMax;
            if (dirX >= 0.0D) {
                tMin = (test.getMinX() - startX) * divX;
                tMax = (test.getMaxX() - startX) * divX;
                hitBlockFaceMin = BlockFace.WEST;
                hitBlockFaceMax = BlockFace.EAST;
            } else {
                tMin = (test.getMaxX() - startX) * divX;
                tMax = (test.getMinX() - startX) * divX;
                hitBlockFaceMin = BlockFace.EAST;
                hitBlockFaceMax = BlockFace.WEST;
            }

            double tyMin;
            double tyMax;
            BlockFace hitBlockFaceYMin;
            BlockFace hitBlockFaceYMax;
            if (dirY >= 0.0D) {
                tyMin = (test.getMinY() - startY) * divY;
                tyMax = (test.getMaxY() - startY) * divY;
                hitBlockFaceYMin = BlockFace.DOWN;
                hitBlockFaceYMax = BlockFace.UP;
            } else {
                tyMin = (test.getMaxY() - startY) * divY;
                tyMax = (test.getMinY() - startY) * divY;
                hitBlockFaceYMin = BlockFace.UP;
                hitBlockFaceYMax = BlockFace.DOWN;
            }

            if (!(tMin > tyMax) && !(tMax < tyMin)) {
                if (tyMin > tMin) {
                    tMin = tyMin;
                    hitBlockFaceMin = hitBlockFaceYMin;
                }

                if (tyMax < tMax) {
                    tMax = tyMax;
                    hitBlockFaceMax = hitBlockFaceYMax;
                }

                double tzMin;
                double tzMax;
                BlockFace hitBlockFaceZMin;
                BlockFace hitBlockFaceZMax;
                if (dirZ >= 0.0D) {
                    tzMin = (test.getMinZ() - startZ) * divZ;
                    tzMax = (test.getMaxZ() - startZ) * divZ;
                    hitBlockFaceZMin = BlockFace.NORTH;
                    hitBlockFaceZMax = BlockFace.SOUTH;
                } else {
                    tzMin = (test.getMaxZ() - startZ) * divZ;
                    tzMax = (test.getMinZ() - startZ) * divZ;
                    hitBlockFaceZMin = BlockFace.SOUTH;
                    hitBlockFaceZMax = BlockFace.NORTH;
                }

                if (!(tMin > tzMax) && !(tMax < tzMin)) {
                    if (tzMin > tMin) {
                        tMin = tzMin;
                        hitBlockFaceMin = hitBlockFaceZMin;
                    }

                    if (tzMax < tMax) {
                        tMax = tzMax;
                        hitBlockFaceMax = hitBlockFaceZMax;
                    }

                    if (tMax < 0.0D) {
                        return null;
                    }

                    double dSq = start.distanceSquared(hitPosition);
                    if (tMin * tMin > dSq) {
                        return null;
                    } else {
                        BlockFace hitBlockFace;
                        if (tMin < 0.0D) {
                            hitBlockFace = hitBlockFaceMax;
                        } else {
                            hitBlockFace = hitBlockFaceMin;
                        }

                        return new ResultEntry(new RayTraceResult(hitPosition, entity, hitBlockFace), dSq);
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    @Value
    private static class ResultEntry implements Comparable<ResultEntry> {
        RayTraceResult result;
        double distance;

        @Override
        public int compareTo(@NotNull MathUtils.ResultEntry o) {
            return Double.compare(distance, o.distance);
        }
    }

    private static  List<ResultEntry> rayTraceInternalIterative(List<Entity> sampleEntities, Predicate<Entity> filter,
                                                           Vector origin, Vector direction, Vector endpoint, int entityCap) {
        List<ResultEntry> results = new ArrayList<>();
        PriorityQueue<ResultEntry> chain = new PriorityQueue<>();

        while(sampleEntities.size() > 0) {
            for(int i = sampleEntities.size() - 1; i > -1; i--) {
                Entity candidate = sampleEntities.get(i);

                if(filter.test(candidate)) { //search for hit
                    ResultEntry hit = fastRayTrace(candidate, origin, direction, endpoint);

                    if(hit != null) {
                        for(int j = sampleEntities.size() - 1; j > -1; j--) { //search backwards for closer entities
                            if(j != i) { //don't test current candidate

                            }
                        }
                    }
                    else if() {

                    }
                }
                else {
                    sampleEntities.remove(i);
                }
            }
        }

        return results;
    }
}
