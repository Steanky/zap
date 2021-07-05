package io.github.zap.zombies.game.mob.goal;

import io.github.zap.arenaapi.pathfind.PathDestination;
import io.github.zap.arenaapi.pathfind.PathHandler;
import io.github.zap.arenaapi.pathfind.PathOperation;
import io.github.zap.arenaapi.pathfind.PathResult;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import net.minecraft.server.v1_16_R3.*;

import java.util.Collections;

public class OptimizedMeleeAttack extends BasicMetadataPathfinder {
    private final double speed;
    private final int attackInterval;
    private final float attackReach;
    private int navigationCounter;
    private int attackTimer;

    public OptimizedMeleeAttack(AbstractEntity entity, AttributeValue[] attributes, double speed, int attackInterval,
                                float attackReach) {
        super(entity, attributes);
        this.speed = speed;
        this.attackInterval = attackInterval;
        this.attackReach = attackReach;
    }

    @Override
    public boolean canStart() {
        Entity target = getHandle().getGoalTarget();
        return target != null && !target.isSpectator() && !((EntityHuman)target).isCreative();
    }

    @Override
    public boolean stayActive() {
        Entity target = getHandle().getGoalTarget();

        if(target == null) {
            return false;
        }
        else {
            return !(target instanceof EntityHuman) || !target.isSpectator() && !((EntityHuman)target).isCreative();
        }
    }

    @Override
    public void onStart() {
        this.getHandle().setAggressive(true);
        this.navigationCounter = 0;
        this.attackTimer = 0;
    }

    @Override
    public void onEnd() {
        this.getHandle().setAggressive(false);
    }

    @Override
    public void doTick() {
        EntityLiving target = this.getHandle().getGoalTarget();
        if(target != null) {
            this.getHandle().getControllerLook().a(target, 30.0F, 30.0F);
            this.navigationCounter = Math.max(this.navigationCounter - 1, 0);

            if (this.navigationCounter <= 0) {
                //randomly offset the delay
                this.navigationCounter = 4 + this.getHandle().getRandom().nextInt(17);

                getHandler().queueOperation(PathOperation.forEntityWalking(getHandle().getBukkitEntity(),
                        Collections.singleton(PathDestination.fromEntity(target.getBukkitEntity(), true)),
                        5), target.getWorld().getWorld());

                PathHandler.Entry entry = getHandler().takeResult();

                PathResult pathResult = null;
                if(entry != null) {
                    pathResult = entry.result();
                    getNavigator().navigateAlongPath(pathResult.toPathEntity(), speed);
                }

                if(pathResult != null) {
                    int nodes = pathResult.pathNodes().size();
                    if(nodes >= 100) {
                        navigationCounter += nodes / 5;
                    }
                }
            }

            attackTimer = Math.max(attackTimer - 1, 0);
            tryAttack(target);
        }
    }

    private void tryAttack(EntityLiving target) {
        if(this.attackTimer <= 0) {
            if(this.getHandle().h(target.locX(), target.locY(), target.locZ()) <= this.checkDistance(target)) {
                this.resetAttackTimer();
                this.getHandle().swingHand(EnumHand.MAIN_HAND);
                this.getHandle().attackEntity(target);
            }
        }
    }

    private void resetAttackTimer() {
        this.attackTimer = attackInterval;
    }

    private double checkDistance(EntityLiving target) {
        return (this.getHandle().getWidth() * attackReach * this.getHandle().getWidth() * attackReach + target.getWidth());
    }
}
