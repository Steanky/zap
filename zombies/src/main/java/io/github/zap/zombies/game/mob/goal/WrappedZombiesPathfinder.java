package io.github.zap.zombies.game.mob.goal;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import lombok.Getter;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.GenericAttributes;
import net.minecraft.server.v1_16_R3.PathfinderGoal;
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

    private int attemptRetarget = 19;
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

        if(target == null && ++attemptRetarget == 20) {
            target = getProxy().findClosest(getHandle(), arena, ZombiesPlayer::isAlive);
            if(target != null) {
                EntityPlayer targetPlayer = ((CraftPlayer)target.getPlayer()).getHandle();

                if(targetPlayer != null) {
                    getHandle().setGoalTarget(targetPlayer, EntityTargetEvent.TargetReason.CUSTOM, true);
                }
                else {
                    Zombies.warning("targetPlayer is null; this should be impossible");
                    return false;
                }

                attemptRetarget = 0;
                return wrappedGoal.a();
            }

            return false;
        }

        return wrappedGoal.a();
    }

    @Override
    public boolean canEnd() {
        return !arena.runAI() && !wrappedGoal.b();
    }

    @Override
    public void onStart() {
        wrappedGoal.c();
    }

    @Override
    public void onEnd() {
        wrappedGoal.d();
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
