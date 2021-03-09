package io.github.zap.zombies.game.mob.goal.mythicmobs;

import io.github.zap.zombies.game.mob.goal.OptimizedMeleeAttack;
import io.github.zap.zombies.game.mob.goal.WrappedZombiesPathfinder;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Sound;

import java.util.EnumSet;

@MythicAIGoal(
        name = "unboundedMeleeAttack"
)
public class WrappedMeleeAttack extends MythicWrapper {
    private final double speed;
    private final int attackInterval;
    private final float attackReach;

    public WrappedMeleeAttack(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);
        speed = mlc.getDouble("speed", 1);
        attackInterval = mlc.getInteger("attackTicks", 20);
        attackReach = mlc.getFloat("attackReach", 2);
    }

    @Override
    public PathfinderGoal create() {
        return new WrappedZombiesPathfinder(entity, new OptimizedMeleeAttack((EntityCreature)getHandle(),
                speed, attackInterval, attackReach), getRetargetInterval(),
                new WrappedZombiesPathfinder.AttributeValue(GenericAttributes.ATTACK_DAMAGE, 2.0D),
                new WrappedZombiesPathfinder.AttributeValue(GenericAttributes.ATTACK_KNOCKBACK, 0.0D));
    }

    @Override
    public boolean isValid() {
        return entity.isCreature();
    }
}
