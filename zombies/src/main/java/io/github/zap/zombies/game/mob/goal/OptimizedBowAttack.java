package io.github.zap.zombies.game.mob.goal;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.proxy.ZombiesNMSProxy;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;

import java.util.EnumSet;

/**
 * Optimized version of the vanilla pathfinding for strafing & shooting.
 * @param <T> The type of the mob that's shooting arrows
 */
public class OptimizedBowAttack<T extends EntityMonster & IRangedEntity> extends PathfinderGoal {
    private final ZombiesNMSProxy proxy;

    private final T self;
    private final double speed;
    private final int attackInterval;
    private final float shootDistanceSquared;
    private final int targetDeviation;
    private int attackTimer = -1;
    private int drawTimer;
    private boolean strafeB;
    private boolean strafeA;
    private int strafeTimer = -1;
    private int navigationCounter = 0;

    private PathEntity currentPath;

    private double distanceToTargetSquared;
    private boolean hasSight;

    public OptimizedBowAttack(T self, double speed, int attackInterval, float shootDistance, int targetDeviation) {
        this.self = self;
        this.speed = speed;
        this.attackInterval = attackInterval;
        this.shootDistanceSquared = shootDistance * shootDistance;
        this.targetDeviation = targetDeviation;
        this.a(EnumSet.of(Type.MOVE, Type.LOOK));

        proxy = Zombies.getInstance().getNmsProxy();
        navigationCounter = self.getRandom().nextInt(5);
    }

    public boolean a() {
        return true;
    }

    public boolean b() {
        Entity target = self.getGoalTarget();

        if(target == null) {
            return false;
        }
        else {
            return !(target instanceof EntityHuman) || !target.isSpectator() && !((EntityHuman)target).isCreative();
        }
    }

    public void c() {
        super.c();
        this.self.setAggressive(true);
    }

    public void d() {
        super.d();
        this.self.setAggressive(false);
        this.drawTimer = 0;
        this.attackTimer = -1;
        this.self.clearActiveItem();
    }

    public void e() {
        EntityLiving target = this.self.getGoalTarget();
        if (target != null) {
            this.navigationCounter = Math.max(this.navigationCounter - 1, 0);
            if (this.navigationCounter <= 0) {
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

                distanceToTargetSquared = this.self.h(target.locX(), target.locY(), target.locZ());
                hasSight = this.self.getEntitySenses().a(target);
            }

            if(currentPath != null) {
                proxy.moveAlongPath(self, currentPath, this.speed);

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
                    if ((double)this.self.getRandom().nextFloat() < 0.3D) {
                        this.strafeB = !this.strafeB;
                    }

                    if ((double)this.self.getRandom().nextFloat() < 0.3D) {
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

                    this.self.getControllerMove().a(this.strafeA ? -0.5F : 0.5F, this.strafeB ? 0.5F : -0.5F);
                    this.self.a(target, 30.0F, 30.0F);
                } else {
                    this.self.getControllerLook().a(target, 30.0F, 30.0F);
                }

                if (this.self.isHandRaised()) {
                    if (!hasSight && this.drawTimer < -60) {
                        this.self.clearActiveItem();
                    } else if (hasSight && distanceToTargetSquared < shootDistanceSquared) {
                        int itemStage = this.self.ea();
                        if (itemStage >= 20) {
                            this.self.clearActiveItem();
                            this.self.a(target, ItemBow.a(itemStage));
                            this.attackTimer = this.attackInterval;
                        }
                    }
                } else if (--this.attackTimer <= 0 && this.drawTimer >= -60 && distanceToTargetSquared < shootDistanceSquared) {
                    this.self.c(ProjectileHelper.a(this.self, Items.BOW));
                }
            }
        }
    }
}