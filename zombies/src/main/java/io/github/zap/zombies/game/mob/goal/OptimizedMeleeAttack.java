package io.github.zap.zombies.game.mob.goal;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

public class OptimizedMeleeAttack extends RetargetingPathfinder {

    private final int attackInterval;
    private final double attackReachSquared;
    private int attackTimer;

    public OptimizedMeleeAttack(Mob mob, AttributeValue[] attributes, int retargetTicks, double speed,
                                double targetDeviation, int attackInterval, double attackReachSquared) {
        super(mob, attributes, retargetTicks, speed, targetDeviation);
        this.attackInterval = attackInterval;
        this.attackReachSquared = attackReachSquared;
    }

    @Override
    public boolean isValid() {
        return self instanceof Creature;
    }

    @Override
    public void start() {
        getNmsBridge().entityBridge().setAggressive(self, true);
        this.attackTimer = 0;
    }

    @Override
    public void end() {
        getNmsBridge().entityBridge().setAggressive(self, false);
        self.setTarget(null);
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = self.getTarget();
        if(target != null) {
            attackTimer = Math.max(attackTimer - 1, 0);
            tryAttack(target);
        }
    }

    private void tryAttack(LivingEntity target) {
        if(this.attackTimer <= 0) {
            Location location = target.getLocation();
            if (getNmsBridge().entityBridge().distanceTo(self, location.getX(), location.getY(), location.getZ())
                    <= this.checkDistance(target)) {
                this.resetAttackTimer();
                self.swingMainHand();
                self.attack(target);
            }
        }
    }

    private void resetAttackTimer() {
        this.attackTimer = attackInterval;
    }

    private double checkDistance(LivingEntity target) {
        return (self.getWidth() * self.getWidth() * attackReachSquared + target.getWidth());
    }

}
