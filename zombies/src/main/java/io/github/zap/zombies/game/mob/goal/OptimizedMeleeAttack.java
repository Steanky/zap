package io.github.zap.zombies.game.mob.goal;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.proxy.ZombiesNMSProxy;
import net.minecraft.server.v1_16_R3.*;

import java.util.EnumSet;

/**
 * Effectively a copy of the NMS PathfinderGoalMeleeAttack, but modified so that zombies will not 'pause' when certain
 * situations occur. Should also be significantly faster without deviating  from vanilla behavior, when not configured
 * to do so.
 *
 * Certain unnecessary features, such as checking entity senses (forgetting targets when out of sight) are disabled.
 * This goal will not perform certain checks that are redundant when part of a WrappedZombiesPathfinder, for example,
 * checking if the target is alive.
 *
 * Pathfinding does not use the entity pathfinding supplied by NMS; rather, it uses coordinate pathfinding.
 */
public class OptimizedMeleeAttack extends PathfinderGoal {
    private final ZombiesNMSProxy proxy;

    protected final EntityCreature self;
    private final double speed;
    private final int attackInterval;
    private final float attackReach;
    private final int targetDeviation;
    private int navigationCounter;
    private int attackTimer;

    private PathEntity currentPath;

    public OptimizedMeleeAttack(EntityCreature self, double speed, int attackInterval, float attackReach, int targetDeviation) {
        this.self = self;
        this.speed = speed;
        this.attackInterval = attackInterval;
        this.attackReach = attackReach;
        this.targetDeviation = targetDeviation;
        this.a(EnumSet.of(Type.MOVE, Type.LOOK));

        proxy = Zombies.getInstance().getNmsProxy();
    }

    public boolean a() {
        return true;
    }

    public boolean b() {
        Entity target = self.getGoalTarget();
        return !(target instanceof EntityHuman) || !target.isSpectator() && !((EntityHuman)target).isCreative();
    }

    public void c() {
        this.self.setAggressive(true);
        this.navigationCounter = 0;
        this.attackTimer = 0;
    }

    public void d() {
        this.self.setAggressive(false);
    }

    public void e() {
        EntityLiving target = this.self.getGoalTarget();
        if(target != null) {
            this.self.getControllerLook().a(target, 30.0F, 30.0F);
            this.navigationCounter = Math.max(this.navigationCounter - 1, 0);

            if (this.navigationCounter <= 0) {
                //randomly offset the delay
                this.navigationCounter = 4 + this.self.getRandom().nextInt(17);

                PathEntity path = proxy.calculatePathTo(self, target, targetDeviation);
                if(path != null) {
                    currentPath = path;
                }
                else {
                    navigationCounter += 25;
                }

                if(currentPath != null) {
                    int nodes = currentPath.getPoints().size();
                    if(nodes >= 100) {
                        navigationCounter += nodes / 5;
                    }
                }
            }

            if(currentPath != null) {
                proxy.moveAlongPath(self, currentPath, speed);
                this.attackTimer = Math.max(this.attackTimer - 1, 0);
                this.tryAttack(target);
            }
        }
    }

    private void tryAttack(EntityLiving target) {
        if(this.attackTimer <= 0) {
            if(this.self.h(target.locX(), target.locY(), target.locZ()) <= this.checkDistance(target)) {
                this.resetAttackTimer();
                this.self.swingHand(EnumHand.MAIN_HAND);
                this.self.attackEntity(target);
            }
        }
    }

    private void resetAttackTimer() {
        this.attackTimer = attackInterval;
    }

    private double checkDistance(EntityLiving target) {
        return (this.self.getWidth() * attackReach * this.self.getWidth() * attackReach + target.getWidth());
    }
}
