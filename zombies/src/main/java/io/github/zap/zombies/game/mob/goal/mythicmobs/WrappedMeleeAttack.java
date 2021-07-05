package io.github.zap.zombies.game.mob.goal.mythicmobs;

import io.github.zap.zombies.game.mob.goal.AttributeValue;
import io.github.zap.zombies.game.mob.goal.OptimizedMeleeAttack;
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
    private final AttributeValue[] attributes;

    public WrappedMeleeAttack(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);
        speed = mlc.getDouble("speed", 1);
        attackInterval = mlc.getInteger("attackTicks", 20);
        attackReachSquared = mlc.getFloat("attackReachSquared", 2);
        double knockback = mlc.getDouble("knockback", 0);

        ActiveMob mob = MythicMobs.inst().getAPIHelper().getMythicMobInstance(entity.getBukkitEntity());
        if(mob != null) {
            Entity nmsEntity = getHandle();
            if(nmsEntity instanceof EntityInsentient && !getProxy().hasAttribute((EntityInsentient)nmsEntity, GenericAttributes.ATTACK_DAMAGE)) {
                ActiveMob mythicMob = MythicMobs.inst().getAPIHelper().getMythicMobInstance(entity.getBukkitEntity());

                attributes = new AttributeValue[] {
                        new AttributeValue(GenericAttributes.ATTACK_DAMAGE, mythicMob == null || mythicMob.getDamage() == 0 ? 2f : mythicMob.getDamage()),
                        new AttributeValue(GenericAttributes.ATTACK_KNOCKBACK, knockback)
                };
            }
            else {
                attributes = new AttributeValue[0];
            }
        }
        else {
            attributes = new AttributeValue[0];
        }
    }

    @Override
    public PathfinderGoal create() {
        return new OptimizedMeleeAttack(getEntity(), speed, attackInterval, attackReachSquared);
    }

    @Override
    public boolean isValid() {
        return entity.isCreature();
    }
}
