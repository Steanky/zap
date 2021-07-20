package io.github.zap.zombies.game.mob.goal;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.EnumSet;

public class OptimizedBowAttack extends RetargetingPathfinder {
    private final int attackInterval;
    private final float shootDistanceSquared;
    private int attackTimer = -1;
    private int drawTimer;
    private boolean strafeB;
    private boolean strafeA;
    private int strafeTimer = -1;

    public OptimizedBowAttack(AbstractEntity entity, AttributeValue[] attributes, double speed, int attackInterval,
                              float shootDistance, int targetDeviation) {
        super(entity, attributes, speed, targetDeviation);
        this.attackInterval = attackInterval;
        this.shootDistanceSquared = shootDistance * shootDistance;
        this.a(EnumSet.of(Type.MOVE, Type.LOOK));
    }

    @Override
    public void onStart() {
        self.setAggressive(true);
    }

    @Override
    public void onEnd() {
        this.drawTimer = 0;
        this.attackTimer = -1;
        self.clearActiveItem();
        self.setAggressive(false);
        self.setGoalTarget(null, EntityTargetEvent.TargetReason.CUSTOM, false);
    }

    @Override
    public void doTick() {
        super.doTick();

        EntityLiving target = self.getGoalTarget();

        if (target != null) {
            double distanceToTargetSquared = self.h(target.locX(), target.locY(), target.locZ());
            boolean hasSight = self.getEntitySenses().a(target);

            boolean bowPartiallyDrawn = this.drawTimer > 0;
            if (hasSight != bowPartiallyDrawn) {
                this.drawTimer = 0;
            }

            if (hasSight) {
                ++this.drawTimer;
            } else {
                --this.drawTimer;
            }

            if (!(distanceToTargetSquared > (double)this.shootDistanceSquared) && this.drawTimer >= 20) {
                ++this.strafeTimer;
            } else {
                this.strafeTimer = -1;
            }

            if (this.strafeTimer >= 20) {
                if ((double) self.getRandom().nextFloat() < 0.3D) {
                    this.strafeB = !this.strafeB;
                }

                if ((double) self.getRandom().nextFloat() < 0.3D) {
                    this.strafeA = !this.strafeA;
                }

                this.strafeTimer = 0;
            }

            if (this.strafeTimer > -1) {
                if (distanceToTargetSquared > (double)(this.shootDistanceSquared * 0.75F)) {
                    this.strafeA = false;
                } else if (distanceToTargetSquared < (double)(this.shootDistanceSquared * 0.25F)) {
                    this.strafeA = true;
                }

                self.getControllerMove().a(this.strafeA ? -0.5F : 0.5F, this.strafeB ? 0.5F : -0.5F);
                self.a(target, 30.0F, 30.0F);
            } else {
                self.getControllerLook().a(target, 30.0F, 30.0F);
            }

            if (self.isHandRaised()) {
                if (!hasSight && this.drawTimer < -60) {
                    self.clearActiveItem();
                } else if (hasSight && distanceToTargetSquared < shootDistanceSquared) {
                    int itemStage = self.ea();
                    if (itemStage >= 20) {
                        self.clearActiveItem();
                        ((IRangedEntity)self).a(target, ItemBow.a(itemStage));
                        this.attackTimer = this.attackInterval;
                    }
                }
            } else if (--this.attackTimer <= 0 && this.drawTimer >= -60 && distanceToTargetSquared < shootDistanceSquared) {
                self.c(ProjectileHelper.a(self, Items.BOW));
            }
        }
    }
}
