package io.github.zap.arenaapi.v1_17_R1.pathfind;

import com.google.common.math.DoubleMath;
import io.github.zap.arenaapi.nms.common.pathfind.MobNavigator;
import io.github.zap.arenaapi.nms.common.pathfind.PathEntityWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Stream;

public class MobNavigator_v1_17_R1 extends GroundPathNavigation implements MobNavigator {
    public MobNavigator_v1_17_R1(Mob entityinsentient, Level world) {
        super(entityinsentient, world);
    }

    @Override
    public void navigateAlongPath(@NotNull PathEntityWrapper pathEntityWrapper, double speed) {
        Path newPath = ((PathEntityWrapper_v1_17_R1)pathEntityWrapper).pathEntity();

        if(path != null) {
            this.path = null;

            Vec3 currentPos = mob.position();

            Node entityPoint = new Node(NumberConversions.floor(currentPos.x),
                    NumberConversions.floor(currentPos.y), NumberConversions.floor(currentPos.z));

            for(int i = 0; i < newPath.getNodeCount(); i++) {
                Node sample = newPath.getNode(i); //(pr)

                float distanceSquared = sample.distanceTo(entityPoint);
                if(DoubleMath.fuzzyCompare(distanceSquared, 1, Vector.getEpsilon()) <= 0) {
                    int nextIndex = i + 1;
                    if(nextIndex < newPath.getNodeCount()) {
                        Node nextPoint = newPath.getNode(nextIndex);
                        float distanceSquaredToNext = nextPoint.distanceTo(entityPoint);

                        if(DoubleMath.fuzzyCompare(distanceSquaredToNext, distanceSquared, Vector.getEpsilon()) <= 0) {
                            i = nextIndex;
                        }
                    }

                    newPath.setNextNodeIndex(i);
                    moveTo(newPath, speed);
                    return;
                }
            }
        }

        moveTo(newPath, speed);
    }

    @Override
    public boolean moveTo(@javax.annotation.Nullable Path path, double speed) {
        if (path == null) {
            this.path = null;
            return false;
        } else {
            this.path = path;

            if (this.isDone()) {
                return false;
            } else {
                this.trimPath();
                if (this.path.getNodeCount() <= 0) {
                    return false;
                } else {
                    this.speedModifier = speed;
                    Vec3 vec3d = this.getTempMobPos();
                    this.lastStuckCheck = this.tick;
                    this.lastStuckCheckPos = vec3d;
                    return true;
                }
            }
        }
    }

    @Override
    protected boolean canUpdatePath() {
        return true;
    }

    @Override
    public Path createPath(BlockPos blockposition, int i) {
        return path;
    }

    @Override
    public Path createPath(Entity entity, int i) {
        return path;
    }

    @Override
    protected boolean canMoveDirectly(Vec3 vec3d, Vec3 vec3d1, int i, int j, int k) {
        return true;
    }

    @Override
    protected boolean hasValidPathType(BlockPathTypes pathtype) {
        return true;
    }

    @Override
    public void setCanPassDoors(boolean flag) { }

    @Override
    public boolean canOpenDoors() {
        return true;
    }

    @Override
    public void setAvoidSun(boolean flag) { }

    @Override
    public void resetMaxVisitedNodesMultiplier() { }

    @Override
    public void setMaxVisitedNodesMultiplier(float f) { }

    @Override
    public void setSpeedModifier(double d0) {
        super.setSpeedModifier(d0);
    }

    @Override
    public boolean hasDelayedRecomputation() {
        return false;
    }

    @Override
    public void recomputePath() { }

    @Nullable
    @Override
    public Path createPath(Stream<BlockPos> stream, int i) {
        return path;
    }

    @Nullable
    @Override
    public Path createPath(Set<BlockPos> set, int i) {
        return path;
    }

    @Nullable
    @Override
    public Path a(BlockPos blockposition, Entity target, int i) {
        return path;
    }

    @Nullable
    @Override
    protected Path createPath(Set<BlockPos> set, int i, boolean flag, int j) {
        return path;
    }

    @Nullable
    @Override
    protected Path createPath(Set<BlockPos> set, Entity target, int i, boolean flag, int j) {
        return path;
    }

    @Override
    public boolean moveTo(double d0, double d1, double d2, double d3) {
        return false;
    }

    @Override
    public boolean moveTo(Entity entity, double d0) {
        return false;
    }

    @Nullable
    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public void stop() { }

    @Override
    protected boolean isInLiquid() { return false; }

    @Override
    public boolean isStableDestination(BlockPos blockposition) {
        return true;
    }

    @Override
    public NodeEvaluator getNodeEvaluator() {
        return null;
    }

    @Override
    public void setCanFloat(boolean flag) { }

    @Override
    public boolean canFloat() {
        return false;
    }

    @Override
    public void recomputePath(BlockPos blockposition) { }

    @Override
    protected void trimPath() { }
}
