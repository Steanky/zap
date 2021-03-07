package io.github.zap.zombies.game.mob.goal;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.mob.goal.mythicmobs.WrappedMeleeAttack;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import lombok.Getter;
import net.minecraft.server.v1_16_R3.GenericAttributes;
import net.minecraft.server.v1_16_R3.PathfinderGoal;
import net.minecraft.server.v1_16_R3.PathfinderGoalMeleeAttack;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * Generic implementation for a pathfinder that is based off of vanilla AI rather than an entirely custom goal. The
 * AI's target is only selected from current, living players in the ZombiesArena. GenericAttributes.FOLLOW_RANGE is set
 * to Double.MAX_VALUE, which makes the AI high iq.
 */
public class WrappedZombiesPathfinder extends ZombiesPathfinder {
    private final int retargetInterval;

    @Getter
    private final PathfinderGoal wrappedGoal;

    @Getter
    private ZombiesArena arena;

    @Getter
    private ZombiesPlayer target;

    private int locateInitial = 19;
    private int counter;

    public WrappedZombiesPathfinder(AbstractEntity entity, PathfinderGoal wrappedGoal, int retargetInterval) {
        super(entity, Zombies.ARENA_METADATA_NAME);
        this.wrappedGoal = wrappedGoal;
        this.retargetInterval = retargetInterval;
        getProxy().setAttributeFor(getHandle(), GenericAttributes.FOLLOW_RANGE, Double.MAX_VALUE);
        counter = retargetInterval > 0 ? getHandle().getRandom().nextInt(retargetInterval) : -1;
    }

    @Override
    public boolean canStart() {
        if(arena == null) {
            arena = getMetadata(Zombies.ARENA_METADATA_NAME);
        }

        if(target == null) { //if our target is null, periodically keep trying to find it
            if(++locateInitial == 20) {
                locateInitial = 0;
                target = getProxy().findClosest(getHandle(), arena, ZombiesPlayer::isAlive);

                if(target != null) {
                    getHandle().setGoalTarget(((CraftPlayer)target.getPlayer()).getHandle(), EntityTargetEvent.TargetReason.CUSTOM, true);
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

        Zombies.info("WrappedZombiesPathfinder onEnd() called");
    }

    @Override
    public void doTick() {
        if(counter > -1 && ++counter == retargetInterval) {
            target = getProxy().findClosest(getHandle(), arena, ZombiesPlayer::isAlive);
            getHandle().setGoalTarget(((CraftPlayer)target.getPlayer()).getHandle());
            counter = 0;
        }

        wrappedGoal.e();
    }
}
