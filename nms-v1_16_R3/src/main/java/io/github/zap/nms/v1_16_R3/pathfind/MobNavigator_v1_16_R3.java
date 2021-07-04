package io.github.zap.nms.v1_16_R3.pathfind;

import io.github.zap.nms.common.pathfind.MobNavigator;
import io.github.zap.nms.common.pathfind.PathEntityWrapper;
import net.minecraft.server.v1_16_R3.*;
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
        c = ((PathEntityWrapper_v1_16_R3)pathEntityWrapper).pathEntity();
        super.a(c, speed);
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
        return false;
    }

    @Override
    public boolean a(@Nullable PathEntity pathentity, double d0) {
        return false;
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
    protected void a(Vec3D vec3d) {
        //idk what this does
        //super.a(vec3d);
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

    @Override
    public void c() {
        ++this.e;

        if (!this.m()) {
            Vec3D vec3d;
            if (this.a()) {
                System.out.println("continueFollowingPath, at valid position");
                this.l();
            } else if (this.c != null && !this.c.c()) {
                vec3d = this.b();
                Vec3D vec3d1 = this.c.a(this.a);
                if (vec3d.y > vec3d1.y && !this.a.isOnGround() && MathHelper.floor(vec3d.x) == MathHelper.floor(vec3d1.x) && MathHelper.floor(vec3d.z) == MathHelper.floor(vec3d1.z)) {
                    this.c.a();
                }
            }

            PacketDebug.a(this.b, this.a, this.c, this.l);
            if (!this.m()) {
                System.out.println("Not idle, moving to node");
                vec3d = this.c.a(this.a);
                BlockPosition blockposition = new BlockPosition(vec3d);
                this.a.getControllerMove().a(vec3d.x, this.b.getType(blockposition.down()).isAir() ? vec3d.y : PathfinderNormal.a(this.b, blockposition), vec3d.z, this.d);
            }
        }
    }
}
