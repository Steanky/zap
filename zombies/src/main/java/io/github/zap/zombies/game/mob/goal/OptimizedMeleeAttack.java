package io.github.zap.zombies.game.mob.goal;

import io.github.zap.arenaapi.pathfind.PathResult;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.event.entity.EntityTargetEvent;

public class OptimizedMeleeAttack extends RetargetingPathfinder {
    private final int attackInterval;
    private final float attackReach;
    private int navigationCounter;
    private int attackTimer;

    public OptimizedMeleeAttack(AbstractEntity entity, AttributeValue[] attributes, double speed, int attackInterval,
                                float attackReach, int targetDeviation) {
        super(entity, attributes, speed, targetDeviation);
        this.attackInterval = attackInterval;
        this.attackReach = attackReach;
    }

    @Override
    public void onStart() {
        self.setAggressive(true);
        this.navigationCounter = self.getRandom().nextInt(10);
        this.attackTimer = 0;
    }

    @Override
    public void onEnd() {
        self.setAggressive(false);
        self.setGoalTarget(null, EntityTargetEvent.TargetReason.CUSTOM, false);
    }

    @Override
    public void doTick() {
        EntityLiving target = self.getGoalTarget();
        if(target != null) {
            self.getControllerLook().a(target, 30.0F, 30.0F);
            this.navigationCounter = Math.max(this.navigationCounter - 1, 0);

            PathResult result = null;
            if (this.navigationCounter <= 0) {
                //randomly offset the navigation so we don't flood the pathfinder
                this.navigationCounter = 4 + self.getRandom().nextInt(18);
                result = retarget();
            }

            if(result != null) {
                setPath(result);

                int nodes = result.pathNodes().size();
                if(nodes >= 100) {
                    navigationCounter += nodes / 5;
                }
            }

            attackTimer = Math.max(attackTimer - 1, 0);
            tryAttack(target);
        }
    }

    private void tryAttack(EntityLiving target) {
        if(this.attackTimer <= 0) {
            if(self.h(target.locX(), target.locY(), target.locZ()) <= this.checkDistance(target)) {
                this.resetAttackTimer();
                self.swingHand(EnumHand.MAIN_HAND);
                self.attackEntity(target);
            }
        }
    }

    private void resetAttackTimer() {
        this.attackTimer = attackInterval;
    }

    private double checkDistance(EntityLiving target) {
        return (self.getWidth() * attackReach * self.getWidth() * attackReach + target.getWidth());
    }
}
