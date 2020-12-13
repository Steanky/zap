package io.github.zap.zombies.game.mob;

import net.minecraft.server.v1_16_R3.*;

import java.util.EnumSet;

public class PathfinderGoalMeleeAttack extends PathfinderGoal {
    protected final EntityCreature entity;
    private final double speedModifier;
    private final boolean unknown;
    private PathEntity pathToTarget;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int counter;
    private int i;
    private final int j = 20;
    private long validationTime;

    public PathfinderGoalMeleeAttack(EntityCreature entity, double speedModifier, boolean unknown) {
        this.entity = entity;
        this.speedModifier = speedModifier;
        this.unknown = unknown;
        this.a(EnumSet.of(Type.MOVE, Type.LOOK));
    }

    public boolean a() {
        long time = this.entity.world.getTime();
        if (time - this.validationTime < 20L) {
            return false;
        } else {
            this.validationTime = time;
            EntityLiving targetEntity = this.entity.getGoalTarget();

            if (targetEntity == null) {
                return false;
            } else if (!targetEntity.isAlive()) {
                return false;
            } else {
                this.pathToTarget = this.entity.getNavigation().a(targetEntity, 0);
                if (this.pathToTarget != null) {
                    return true;
                } else {
                    return this.boundsWidth(targetEntity) >= this.entity.h(targetEntity.locX(), targetEntity.locY(),
                            targetEntity.locZ());
                }
            }
        }
    }

    public boolean b() {
        EntityLiving targetEntity = this.entity.getGoalTarget();
        if (targetEntity == null) {
            return false;
        } else if (!targetEntity.isAlive()) {
            return false;
        } else if (!this.unknown) {
            return !this.entity.getNavigation().m();
        } else {
            return !(targetEntity instanceof EntityHuman) || !targetEntity.isSpectator() &&
                    !((EntityHuman)targetEntity).isCreative();
        }
    }

    public void c() {
        this.entity.getNavigation().a(this.pathToTarget, this.speedModifier);
        this.entity.setAggressive(true);
        this.counter = 0;
        this.i = 0;
    }

    public void d() {
        EntityLiving var0 = this.entity.getGoalTarget();
        if (!IEntitySelector.e.test(var0)) {
            this.entity.setGoalTarget((EntityLiving)null);
        }

        this.entity.setAggressive(false);
        this.entity.getNavigation().o();
    }

    public void e() { //tick
        EntityLiving targetEntity = this.entity.getGoalTarget();
        this.entity.getControllerLook().a(targetEntity, 30.0F, 30.0F);
        double distance = this.entity.h(targetEntity.locX(), targetEntity.locY(), targetEntity.locZ());
        this.counter = Math.max(this.counter - 1, 0);
        if ((this.unknown || this.entity.getEntitySenses().a(targetEntity)) && this.counter <= 0 &&
                (this.targetX == 0.0D && this.targetY == 0.0D && this.targetZ == 0.0D ||
                        targetEntity.h(this.targetX, this.targetY, this.targetZ) >= 1.0D ||
                        this.entity.getRandom().nextFloat() < 0.05F)) {
            this.targetX = targetEntity.locX();
            this.targetY = targetEntity.locY();
            this.targetZ = targetEntity.locZ();
            this.counter = 4 + this.entity.getRandom().nextInt(7);
            if (distance > 1024.0D) {
                this.counter += 10;
            } else if (distance > 256.0D) {
                this.counter += 5;
            }

            if (!this.entity.getNavigation().a(targetEntity, this.speedModifier)) {
                this.counter += 15;
            }
        }

        this.i = Math.max(this.i - 1, 0);
        this.tryAttack(targetEntity, distance);
    }

    protected void tryAttack(EntityLiving target, double distance) {
        double width = this.boundsWidth(target);
        if (distance <= width && this.i <= 0) {
            this.g();
            this.entity.swingHand(EnumHand.MAIN_HAND);
            this.entity.attackEntity(target);
        }
    }

    protected void g() {
        this.i = 20;
    }

    protected boolean h() {
        return this.i <= 0;
    }

    protected int j() {
        return this.i;
    }

    protected int k() {
        return 20;
    }

    protected double boundsWidth(EntityLiving target) {
        return (this.entity.getWidth() * 2.0F * this.entity.getWidth() * 2.0F + target.getWidth());
    }
}