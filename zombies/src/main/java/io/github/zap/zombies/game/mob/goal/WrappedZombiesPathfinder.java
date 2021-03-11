package io.github.zap.zombies.game.mob.goal;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import lombok.Getter;
import lombok.Value;
import net.minecraft.server.v1_16_R3.AttributeBase;
import net.minecraft.server.v1_16_R3.EntityInsentient;
import net.minecraft.server.v1_16_R3.GenericAttributes;
import net.minecraft.server.v1_16_R3.PathfinderGoal;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * Generic implementation for a pathfinder that is based off of vanilla AI rather than an entirely custom goal. The
 * AI's target is only selected from current, living players in the ZombiesArena.
 */
public class WrappedZombiesPathfinder extends ZombiesPathfinder {
    @Value
    public static class AttributeValue {
        AttributeBase attribute;
        double value;
    }

    private final int retargetInterval;

    @Getter
    private final PathfinderGoal wrappedGoal;

    @Getter
    private ZombiesArena arena;

    @Getter
    private ZombiesPlayer target;

    private int locateInitial = 19;
    private int counter;

    public WrappedZombiesPathfinder(AbstractEntity entity, PathfinderGoal wrappedGoal, int retargetInterval,
                                    AttributeValue...values) {
        super(entity, Zombies.ARENA_METADATA_NAME);
        this.wrappedGoal = wrappedGoal;
        this.retargetInterval = retargetInterval;
        getProxy().setDoubleFor(getHandle(), GenericAttributes.FOLLOW_RANGE, Float.MAX_VALUE);

        EntityInsentient entityInsentient = getHandle();
        entityInsentient.getNavigation().a(Float.MAX_VALUE);

        counter = retargetInterval > 0 ? entityInsentient.getRandom().nextInt(retargetInterval) : -1;

        for(AttributeValue value : values) {
            getProxy().setDoubleFor(entityInsentient, value.attribute, value.value);
        }
    }

    @Override
    public boolean canStart() {
        if(arena == null) {
            arena = getMetadata(Zombies.ARENA_METADATA_NAME);
        }

        if(target == null) { //if our target is null, periodically keep trying to find it
            if(++locateInitial == 20) {
                locateInitial = 0;
                target = getProxy().findClosest(getHandle(), arena, 0, ZombiesPlayer::isAlive);

                if(target != null) {
                    getHandle().setGoalTarget(((CraftPlayer)target.getPlayer()).getHandle(),
                            EntityTargetEvent.TargetReason.CUSTOM, true);
                    return wrappedGoal.a();
                }
            }

            return false;
        }

        return wrappedGoal.a();
    }

    @Override
    public boolean stayActive() {
        return arena.runAI() && target.isAlive() && wrappedGoal.b();
    }

    @Override
    public void onStart() {
        wrappedGoal.c();
    }

    @Override
    public void onEnd() {
        getHandle().setGoalTarget(null, EntityTargetEvent.TargetReason.CUSTOM, true);
        getHandle().getNavigation().stopPathfinding(); //necessary for some aigoals to work right
        target = null;
        locateInitial = 19;
        wrappedGoal.d();
    }

    @Override
    public void doTick() {
        /*
        periodic target recalculation; this by default doesn't happen
         */
        if((counter > -1 && ++counter == retargetInterval)) {
            target = getProxy().findClosest(getHandle(), arena, 0, ZombiesPlayer::isAlive);

            if(target != null) {
                getHandle().setGoalTarget(((CraftPlayer)target.getPlayer()).getHandle());
            }

            counter = 0;
        }

        /*
        if some NMS pathfinder sets the goal target to null, and we have a ZombiesPlayer to target, re-set the goal
        target. This generally should not happen, but poorly behaved pathfinders may exist.
         */
        if(target != null && getHandle().getGoalTarget() == null) {
            getHandle().setGoalTarget(((CraftPlayer)target.getPlayer()).getHandle());
        }

        wrappedGoal.e();
    }
}
