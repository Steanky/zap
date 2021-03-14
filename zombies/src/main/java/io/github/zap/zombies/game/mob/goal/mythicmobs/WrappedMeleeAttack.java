package io.github.zap.zombies.game.mob.goal.mythicmobs;

import io.github.zap.zombies.game.mob.goal.OptimizedMeleeAttack;
import io.github.zap.zombies.game.mob.goal.WrappedZombiesPathfinder;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import net.minecraft.server.v1_16_R3.*;

@MythicAIGoal(
        name = "unboundedMeleeAttack"
)
public class WrappedMeleeAttack extends MythicWrapper {
    private final double speed;
    private final int attackInterval;
    private final float attackReachSquared;
    private final int targetDeviation;

    private final WrappedZombiesPathfinder.AttributeValue[] attributes;

    public WrappedMeleeAttack(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);
        speed = mlc.getDouble("speed", 1);
        attackInterval = mlc.getInteger("attackTicks", 20);
        attackReachSquared = mlc.getFloat("attackReachSquared", 2);
        targetDeviation = mlc.getInteger("targetDeviation", 0);
        double knockback = mlc.getDouble("knockback", 0);

        ActiveMob mob = MythicMobs.inst().getAPIHelper().getMythicMobInstance(entity.getBukkitEntity());
        if(mob != null) {
            Entity nmsEntity = getHandle();
            if(nmsEntity instanceof EntityInsentient && !getProxy().hasAttribute((EntityInsentient)nmsEntity, GenericAttributes.ATTACK_DAMAGE)) {
                ActiveMob mythicMob = MythicMobs.inst().getAPIHelper().getMythicMobInstance(entity.getBukkitEntity());

                attributes = new WrappedZombiesPathfinder.AttributeValue[] {
                        new WrappedZombiesPathfinder.AttributeValue(GenericAttributes.ATTACK_DAMAGE,
                                mythicMob == null || mythicMob.getDamage() == 0 ? 2f : mythicMob.getDamage()),
                        new WrappedZombiesPathfinder.AttributeValue(GenericAttributes.ATTACK_KNOCKBACK, knockback)
                };
            }
            else {
                attributes = new WrappedZombiesPathfinder.AttributeValue[0];
            }
        }
        else {
            attributes = new WrappedZombiesPathfinder.AttributeValue[0];
        }
    }

    @Override
    public PathfinderGoal create() {
        return new WrappedZombiesPathfinder(entity, new OptimizedMeleeAttack((EntityCreature)getHandle(),
                speed, attackInterval, attackReachSquared, targetDeviation), getRetargetInterval(), attributes);
    }

    @Override
    public boolean isValid() {
        return entity.isCreature();
    }
}
