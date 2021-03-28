package io.github.zap.arenaapi.pathfind;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftMob;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Performs a PathOperation based on the mob's NMS pathfinder.
 */
class PathOperationAdapted implements PathOperation {
    private final Mob mob;
    private final Set<PathDestination> destinations;
    private PathState state;
    private PathEntity pathEntity;

    //NMS garbage below
    private final PathfinderAbstract c;
    private final PathPoint[] possibleNodes = new PathPoint[32];
    private final int b;
    private final int i;
    private final float f;
    private final Path path = new Path();
    private int j;
    private final int k;
    private final List<Map.Entry<net.minecraft.server.v1_16_R3.PathDestination, BlockPosition>> foundDestinations;
    private PathPoint pathpoint;
    private final List<Map.Entry<net.minecraft.server.v1_16_R3.PathDestination, BlockPosition>> list = new ArrayList<>();

    PathOperationAdapted(@NotNull Mob mob, @NotNull Set<PathDestination> destinations, int giveUpFactor, int tolerance, float maxNodeDistance) {
        this.mob = mob;
        this.destinations = destinations;
        this.state = PathState.INCOMPLETE;

        for(PathDestination destination : destinations) {
            destination.targetNode();
            PathNode node = destination.targetNode();
            list.add(Map.entry(new net.minecraft.server.v1_16_R3.PathDestination(node.toNms()), new BlockPosition(node.x, node.y, node.z)));
        }

        c = ((CraftMob)mob).getHandle().getNavigation().getPathfinder().getPathfinder();
        b = giveUpFactor;
        i = tolerance;
        f = maxNodeDistance;

        pathpoint.e = 0.0F;
        pathpoint.f = this.closestDestination(pathpoint, list);
        pathpoint.g = pathpoint.f;

        this.path.a();
        this.path.a(pathpoint);
        Set<PathPoint> set1 = ImmutableSet.of();
        j = 0;
        foundDestinations = Lists.newArrayListWithExpectedSize(list.size());
        k = this.b;
    }

    @Override
    public boolean step(@NotNull PathfinderContext context) {
        if(this.path.e()) {
            if (++j >= k) {
                state = PathState.FAILED;
                return false;
            }

            PathPoint bestNode = this.path.c();
            bestNode.i = true;

            int nodeCount;
            for(nodeCount = 0; nodeCount < list.size(); ++nodeCount) {
                Map.Entry<net.minecraft.server.v1_16_R3.PathDestination, BlockPosition> entry = list.get(nodeCount);
                net.minecraft.server.v1_16_R3.PathDestination destination = entry.getKey();
                if (bestNode.c(destination) <= (float)i) {
                    destination.e();
                    foundDestinations.add(entry);
                }
            }

            if (foundDestinations.isEmpty()) {
                nodeCount = this.c.a(this.possibleNodes, bestNode);

                for(int i1 = 0; i1 < nodeCount; ++i1) {
                    PathPoint next = this.possibleNodes[i1];
                    float distanceToNextSquared = bestNode.a(next);
                    next.j = bestNode.j + distanceToNextSquared;
                    float f3 = bestNode.e + distanceToNextSquared + next.k;
                    if (next.j < f && (!next.c() || f3 < next.e)) {
                        next.h = bestNode;
                        next.e = f3;
                        next.f = this.closestDestination(next, list) * 1.5F;
                        if (next.c()) {
                            this.path.a(next, next.e + next.f);
                        } else {
                            next.g = next.e + next.f;
                            this.path.a(next);
                        }
                    }
                }
            }
        }

        PathEntity best = null;
        boolean useSet1 = foundDestinations.isEmpty();
        Comparator<PathEntity> comparator = useSet1 ? Comparator.comparingInt(PathEntity::e) : Comparator.comparingDouble(PathEntity::n).thenComparingInt(PathEntity::e);
        Iterator resultIterator = ((List)(useSet1 ? list : foundDestinations)).iterator();

        while(true) {
            PathEntity pathEntity;
            do {
                if (!resultIterator.hasNext()) {
                    pathEntity = best;
                    return true;
                }

                Map.Entry<net.minecraft.server.v1_16_R3.PathDestination, BlockPosition> entry = (Map.Entry)resultIterator.next();
                pathEntity = this.buildPathEntity(entry.getKey().d(), entry.getValue(), !useSet1);
            } while(best != null && comparator.compare(pathEntity, best) >= 0);

            best = pathEntity;
        }
    }

    @Override
    public @NotNull PathState getState() {
        return state;
    }

    @Override
    public @NotNull PathResult getResult() {
        if(pathEntity == null) {
            throw new IllegalStateException("operation not completed; cannot retrieve PathResult");
        }

        return null;
    }

    @Override
    public int desiredIterations() {
        return 0;
    }

    @Override
    public boolean shouldRemove() {
        return false;
    }

    @Override
    public @NotNull World getWorld() {
        return mob.getWorld();
    }

    @Override
    public @NotNull Set<PathDestination> getDestinations() {
        return destinations;
    }

    private float closestDestination(PathPoint pathpoint, List<Map.Entry<net.minecraft.server.v1_16_R3.PathDestination, BlockPosition>> list) {
        float f = 3.4028235E38F;
        int i = 0;

        for(int listSize = list.size(); i < listSize; ++i) {
            net.minecraft.server.v1_16_R3.PathDestination pathdestination =
                    (net.minecraft.server.v1_16_R3.PathDestination)((Map.Entry)list.get(i)).getKey();
            float f1 = pathpoint.a(pathdestination);
            pathdestination.a(f1, pathpoint);
            f = Math.min(f1, f);
        }

        return f;
    }

    private PathEntity buildPathEntity(PathPoint pathpoint, BlockPosition blockposition, boolean flag) {
        List<PathPoint> list = Lists.newArrayList();
        PathPoint pathpoint1 = pathpoint;
        list.add(0, pathpoint);

        while(pathpoint1.h != null) {
            pathpoint1 = pathpoint1.h;
            list.add(0, pathpoint1);
        }

        return new PathEntity(list, blockposition, flag);
    }
}
