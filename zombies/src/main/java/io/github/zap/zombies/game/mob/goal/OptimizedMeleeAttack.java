package io.github.zap.zombies.game.mob.goal;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EnumHand;
import org.bukkit.event.entity.EntityTargetEvent;

public class OptimizedMeleeAttack extends RetargetingPathfinder {
    private final int attackInterval;
    private final float attackReach;
    private int attackTimer;

    public OptimizedMeleeAttack(AbstractEntity entity, AttributeValue[] attributes, int retargetTicks, double speed,
                                int attackInterval, float attackReach, double targetDeviation) {
        super(entity, attributes, retargetTicks, speed, targetDeviation);
        this.attackInterval = attackInterval;
        this.attackReach = attackReach;
    }

    @Override
    public void onStart() {
        self.setAggressive(true);
        this.attackTimer = 0;
    }

    @Override
    public void onEnd() {
        self.setAggressive(false);
        //self.setGoalTarget(null, EntityTargetEvent.TargetReason.CUSTOM, false);
    }

    @Override
    public void doTick() {
        super.doTick();

        EntityLiving target = self.getGoalTarget();
        if(target != null) {
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
