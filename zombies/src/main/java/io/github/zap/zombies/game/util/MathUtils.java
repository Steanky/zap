package io.github.zap.zombies.game.util;

import lombok.Value;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public class MathUtils {
    @Value
    public static class RaycastResult implements Comparable<RaycastResult> {
        Entity hitEntity;
        Vector hitPosition;
        BlockFace hitBlockFace;
        double distanceSquared;

        @Override
        public int compareTo(@NotNull MathUtils.RaycastResult o) {
            return Double.compare(o.distanceSquared, distanceSquared);
        }
    }

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

    public static Queue<RaycastResult> sortedRayTraceEntities(Location origin, Vector direction, double maxDistance,
                                                              int entityCap, Predicate<Entity> filter) {
        Vector originVector = origin.toVector();

        return rayCastInternalSimple((List<Entity>)origin.getWorld()
                .getNearbyEntities(BoundingBox.of(originVector, originVector).expandDirectional(direction.clone()
                        .multiply(maxDistance)), filter), filter, originVector, direction,
                maxDistance * maxDistance, entityCap);
    }

    /*
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
    */

    private static RaycastResult fastRaycast(@NotNull Entity entity, @NotNull Vector start, @NotNull Vector direction,
                                             double lengthSquared) {
        BoundingBox self = entity.getBoundingBox();

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
            tMin = (self.getMinX() - startX) * divX;
            tMax = (self.getMaxX() - startX) * divX;
            hitBlockFaceMin = BlockFace.WEST;
            hitBlockFaceMax = BlockFace.EAST;
        } else {
            tMin = (self.getMaxX() - startX) * divX;
            tMax = (self.getMinX() - startX) * divX;
            hitBlockFaceMin = BlockFace.EAST;
            hitBlockFaceMax = BlockFace.WEST;
        }

        double tyMin;
        double tyMax;
        BlockFace hitBlockFaceYMin;
        BlockFace hitBlockFaceYMax;
        if (dirY >= 0.0D) {
            tyMin = (self.getMinY() - startY) * divY;
            tyMax = (self.getMaxY() - startY) * divY;
            hitBlockFaceYMin = BlockFace.DOWN;
            hitBlockFaceYMax = BlockFace.UP;
        } else {
            tyMin = (self.getMaxY() - startY) * divY;
            tyMax = (self.getMinY() - startY) * divY;
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
                tzMin = (self.getMinZ() - startZ) * divZ;
                tzMax = (self.getMaxZ() - startZ) * divZ;
                hitBlockFaceZMin = BlockFace.NORTH;
                hitBlockFaceZMax = BlockFace.SOUTH;
            } else {
                tzMin = (self.getMaxZ() - startZ) * divZ;
                tzMax = (self.getMinZ() - startZ) * divZ;
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

                if (tMin * tMin > lengthSquared) {
                    return null;
                } else {
                    double t;
                    BlockFace hitBlockFace;
                    if (tMin < 0.0D) {
                        t = tMax;
                        hitBlockFace = hitBlockFaceMax;
                    } else {
                        t = tMin;
                        hitBlockFace = hitBlockFaceMin;
                    }

                    Vector hitPoint = direction.clone().multiply(t).add(start);
                    return new RaycastResult(entity, hitPoint, hitBlockFace, start.distanceSquared(hitPoint));
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static Queue<RaycastResult> rayCastInternalSimple(List<Entity> sampleEntities, Predicate<Entity> filter,
                                                              Vector origin, Vector direction, double lengthSquared,
                                                              int entityCap) {
        Queue<RaycastResult> chain = new PriorityQueue<>();

        for(Entity entity : sampleEntities) {
            if(filter.test(entity)) { //search for hit
                RaycastResult hit = fastRaycast(entity, origin, direction, lengthSquared);

                if(hit != null) {
                    chain.add(hit);

                    if(chain.size() > entityCap) {
                        chain.remove();
                    }
                }
            }
        }

        return chain;
    }
}
