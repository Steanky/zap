package io.github.zap.arenaapi.nms.v1_16_R3.pathfind;

import com.google.common.math.DoubleMath;
import io.github.zap.arenaapi.nms.common.pathfind.MobNavigator;
import io.github.zap.arenaapi.nms.common.pathfind.PathEntityWrapper;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Stream;

public class MobNavigator_v1_16_R3 extends Navigation implements MobNavigator {
    public MobNavigator_v1_16_R3(EntityInsentient entityinsentient, World world) {
        super(entityinsentient, world);
    }

    @Override
    public void navigateAlongPath(@NotNull PathEntityWrapper pathEntityWrapper, double speed) {
        PathEntity newPath = ((PathEntityWrapper_v1_16_R3)pathEntityWrapper).pathEntity();

        if(c != null) {
            this.c = null;

            Vec3D currentPos = getEntity().getPositionVector();

            PathPoint entityPoint = new PathPoint(NumberConversions.floor(currentPos.x),
                    NumberConversions.floor(currentPos.y), NumberConversions.floor(currentPos.z));

            for(int i = 0; i < newPath.e(); i++) {
                PathPoint sample = newPath.a(i); //(pr)

                float distanceSquared = sample.a(entityPoint);
                if(DoubleMath.fuzzyCompare(distanceSquared, 1, Vector.getEpsilon()) <= 0) {
                    int nextIndex = i + 1;
                    if(nextIndex < newPath.e()) {
                        PathPoint nextPoint = newPath.a(nextIndex);
                        float distanceSquaredToNext = nextPoint.a(entityPoint);

                        if(DoubleMath.fuzzyCompare(distanceSquaredToNext, distanceSquared, Vector.getEpsilon()) <= 0
                                && entityPoint.getY() == nextPoint.getY()) {
                            i = nextIndex;
                        }
                    }

                    newPath.c(i);
                    a(newPath, speed);
                    return;
                }
            }
        }

        a(newPath, speed);
    }

    @Override
    public boolean a(@javax.annotation.Nullable PathEntity pathentity, double d0) {
        if (pathentity == null) {
            this.c = null;
            return false;
        } else {
            this.c = pathentity;

            if (this.m()) {
                return false;
            } else {
                this.D_();
                if (this.c.e() <= 0) {
                    return false;
                } else {
                    this.d = d0;
                    Vec3D vec3d = this.b();
                    this.f = this.e;
                    this.g = vec3d;
                    return true;
                }
            }
        }
    }

    @Override
    protected boolean a() {
        return true;
    }

    @Override
    public PathEntity a(BlockPosition blockposition, int i) {
        return c;
    }

    @Override
    public PathEntity a(Entity entity, int i) {
        return c;
    }

    @Override
    protected boolean a(Vec3D vec3d, Vec3D vec3d1, int i, int j, int k) {
        return true;
    }

    @Override
    protected boolean a(PathType pathtype) {
        return true;
    }

    @Override
    public void a(boolean flag) { }

    @Override
    public boolean f() {
        return true;
    }

    @Override
    public void c(boolean flag) { }

    @Override
    public Pathfinder getPathfinder() {
        return null;
    }

    @Override
    public void g() { }

    @Override
    public void a(float f) { }

    @Override
    public void a(double d0) {
        super.a(d0);
    }

    @Override
    public boolean i() {
        return false;
    }

    @Override
    public void j() { }

    @Nullable
    @Override
    public PathEntity a(Stream<BlockPosition> stream, int i) {
        return c;
    }

    @Nullable
    @Override
    public PathEntity a(Set<BlockPosition> set, int i) {
        return c;
    }

    @Nullable
    @Override
    public PathEntity a(BlockPosition blockposition, Entity target, int i) {
        return c;
    }

    @Nullable
    @Override
    protected PathEntity a(Set<BlockPosition> set, int i, boolean flag, int j) {
        return c;
    }

    @Nullable
    @Override
    protected PathEntity a(Set<BlockPosition> set, Entity target, int i, boolean flag, int j) {
        return c;
    }

    @Override
    public boolean a(double d0, double d1, double d2, double d3) {
        return false;
    }

    @Override
    public boolean a(Entity entity, double d0) {
        return false;
    }

    @Override
    public boolean setDestination(@Nullable PathEntity pathentity, double speed) {
        return a(pathentity, speed);
    }

    @Nullable
    @Override
    public PathEntity getPathEntity() {
        return c;
    }

    @Nullable
    @Override
    public PathEntity k() {
        return c;
    }

    @Override
    public void stopPathfinding() { }

    @Override
    public void o() { }

    @Override
    protected boolean p() { return false; }

    @Override
    public boolean a(BlockPosition blockposition) {
        return true;
    }

    @Override
    public PathfinderAbstract q() {
        return null;
    }

    @Override
    public void d(boolean flag) { }

    @Override
    public boolean r() {
        return false;
    }

    @Override
    public void b(BlockPosition blockposition) { }

    @Override
    protected void D_() { }
}
