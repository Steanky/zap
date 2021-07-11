package io.github.zap.zombies.game.mob.goal;

import io.github.zap.arenaapi.pathfind.PathDestination;
import io.github.zap.arenaapi.pathfind.PathOperation;
import io.github.zap.arenaapi.pathfind.PathResult;
import io.github.zap.arenaapi.shadow.io.github.zap.nms.v1_16_R3.pathfind.PathEntityWrapper_v1_16_R3;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.proxy.ZombiesNMSProxy;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.EnumSet;
import java.util.Set;

public class OptimizedBowAttack extends RetargetingPathfinder {
    private final ZombiesNMSProxy proxy;

    private final int attackInterval;
    private final float shootDistanceSquared;
    private final int targetDeviation;
    private int attackTimer = -1;
    private int drawTimer;
    private boolean strafeB;
    private boolean strafeA;
    private int strafeTimer = -1;
    private int navigationCounter;

    private double distanceToTargetSquared;
    private boolean hasSight;

    public OptimizedBowAttack(AbstractEntity entity, AttributeValue[] attributes, double speed, int attackInterval,
                              float shootDistance, int targetDeviation) {
        super(entity, attributes, speed);
        this.attackInterval = attackInterval;
        this.shootDistanceSquared = shootDistance * shootDistance;
        this.targetDeviation = targetDeviation;
        this.a(EnumSet.of(Type.MOVE, Type.LOOK));

        proxy = Zombies.getInstance().getNmsProxy();
        navigationCounter = getHandle().getRandom().nextInt(5);
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
    }

    @Override
    public void onEnd() {
        this.getHandle().setAggressive(false);
        this.drawTimer = 0;
        this.attackTimer = -1;
        this.getHandle().clearActiveItem();
        this.getHandle().setGoalTarget(null, EntityTargetEvent.TargetReason.CUSTOM, false);
    }

    @Override
    public void doTick() {
        EntityInsentient self = getHandle();
        EntityLiving target = self.getGoalTarget();

        if (target != null) {
            this.navigationCounter = Math.max(this.navigationCounter - 1, 0);
            if (this.navigationCounter <= 0) {
                this.navigationCounter = 4 + self.getRandom().nextInt(17);

                PathResult result = retarget();
                if(result != null) {
                    setPath(result);

                    int nodes = result.pathNodes().size();
                    if(nodes >= 100) {
                        navigationCounter += nodes / 5;
                    }
                }
                else {
                    navigationCounter += 25;
                }

                distanceToTargetSquared = self.h(target.locX(), target.locY(), target.locZ());
                hasSight = self.getEntitySenses().a(target);
            }

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
                if ((double)self.getRandom().nextFloat() < 0.3D) {
                    this.strafeB = !this.strafeB;
                }

                if ((double)self.getRandom().nextFloat() < 0.3D) {
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
                        self.a(target, ItemBow.a(itemStage));
                        this.attackTimer = this.attackInterval;
                    }
                }
            } else if (--this.attackTimer <= 0 && this.drawTimer >= -60 && distanceToTargetSquared < shootDistanceSquared) {
                self.c(ProjectileHelper.a(self, Items.BOW));
            }
        }
    }
}
