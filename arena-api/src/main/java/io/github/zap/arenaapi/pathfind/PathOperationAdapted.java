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
    private final PathPoint[] a = new PathPoint[32];
    private final int b;
    private final int i;
    private final float f;
    private final Path path = new Path();
    private int j;
    private final int k;
    private List<Map.Entry<net.minecraft.server.v1_16_R3.PathDestination, BlockPosition>> set2;
    private PathPoint pathpoint;
    private final List<Map.Entry<net.minecraft.server.v1_16_R3.PathDestination, BlockPosition>> list = new ArrayList<>();

    PathOperationAdapted(@NotNull Mob mob, @NotNull Set<PathDestination> destinations, int giveUpFactor, int tolerance, float nodeDistance) {
        this.mob = mob;
        this.destinations = destinations;
        this.state = PathState.INCOMPLETE;

        for(PathDestination destination : destinations) {
            destination.targetNode();
            PathNode node = destination.targetNode();
            PathPoint point = new PathPoint(node.getX(), node.getY(), node.getZ());


            list.add(Map.entry(new net.minecraft.server.v1_16_R3.PathDestination(point), new BlockPosition(node.getX(), node.getY(), node.getZ())));
        }

        c = ((CraftMob)mob).getHandle().getNavigation().getPathfinder().getPathfinder();
        b = giveUpFactor;
        i = tolerance;
        f = nodeDistance;

        pathpoint.e = 0.0F;
        pathpoint.f = this.a(pathpoint, list);
        pathpoint.g = pathpoint.f;

        this.path.a();
        this.path.a(pathpoint);
        Set<PathPoint> set1 = ImmutableSet.of();
        j = 0;
        set2 = Lists.newArrayListWithExpectedSize(list.size());
        k = (int)(this.b);
    }

    @Override
    public boolean step(@NotNull PathfinderContext context) {
        if(this.path.e()) {
            ++j;
            if (j >= k) {
                state = PathState.FAILED;
                return false;
            }

            PathPoint pathpoint1 = this.path.c();
            pathpoint1.i = true;

            int l;
            for(l = 0; l < list.size(); ++l) {
                Map.Entry<net.minecraft.server.v1_16_R3.PathDestination, BlockPosition> entry = list.get(l);
                net.minecraft.server.v1_16_R3.PathDestination pathdestination = entry.getKey();
                if (pathpoint1.c(pathdestination) <= (float)i) {
                    pathdestination.e();
                    set2.add(entry);
                }
            }

            if (!set2.isEmpty()) {
                //called when the mob is within range of its target
                return false;
            }
            else if (pathpoint1.a(pathpoint) < f) {
                l = this.c.a(this.a, pathpoint1);

                for(int i1 = 0; i1 < l; ++i1) {
                    PathPoint pathpoint2 = this.a[i1];
                    float f2 = pathpoint1.a(pathpoint2);
                    pathpoint2.j = pathpoint1.j + f2;
                    float f3 = pathpoint1.e + f2 + pathpoint2.k;
                    if (pathpoint2.j < f && (!pathpoint2.c() || f3 < pathpoint2.e)) {
                        pathpoint2.h = pathpoint1;
                        pathpoint2.e = f3;
                        pathpoint2.f = this.a(pathpoint2, list) * 1.5F;
                        if (pathpoint2.c()) {
                            this.path.a(pathpoint2, pathpoint2.e + pathpoint2.f);
                        } else {
                            pathpoint2.g = pathpoint2.e + pathpoint2.f;
                            this.path.a(pathpoint2);
                        }
                    }
                }
            }
        }

        PathEntity best = null;
        boolean useSet1 = set2.isEmpty();
        Comparator<PathEntity> comparator = useSet1 ? Comparator.comparingInt(PathEntity::e) : Comparator.comparingDouble(PathEntity::n).thenComparingInt(PathEntity::e);
        Iterator var21 = ((List)(useSet1 ? list : set2)).iterator();

        while(true) {
            PathEntity pathEntity;
            do {
                if (!var21.hasNext()) {
                    pathEntity = best;
                    return true;
                }

                Map.Entry<net.minecraft.server.v1_16_R3.PathDestination, BlockPosition> entry = (Map.Entry)var21.next();
                pathEntity = this.a(entry.getKey().d(), entry.getValue(), !useSet1);
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

    private float a(PathPoint pathpoint, List<Map.Entry<net.minecraft.server.v1_16_R3.PathDestination, BlockPosition>> list) {
        float f = 3.4028235E38F;
        int i = 0;

        for(int listSize = list.size(); i < listSize; ++i) {
            net.minecraft.server.v1_16_R3.PathDestination pathdestination = (net.minecraft.server.v1_16_R3.PathDestination)((Map.Entry)list.get(i)).getKey();
            float f1 = pathpoint.a(pathdestination);
            pathdestination.a(f1, pathpoint);
            f = Math.min(f1, f);
        }

        return f;
    }

    private PathEntity a(PathPoint pathpoint, BlockPosition blockposition, boolean flag) {
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
