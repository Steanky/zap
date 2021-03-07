package io.github.zap.zombies.game.mob.goal.mythicmobs;

import io.github.zap.zombies.game.mob.goal.OptimizedMeleeAttack;
import io.github.zap.zombies.game.mob.goal.WrappedZombiesPathfinder;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import net.minecraft.server.v1_16_R3.*;

import java.util.EnumSet;

@MythicAIGoal(
        name = "unboundedMeleeAttack"
)
public class WrappedMeleeAttack extends MythicWrapper {
    private final double speed;
    private final int attackInterval;

    public WrappedMeleeAttack(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);
        speed = mlc.getDouble("speed", 1);
        attackInterval = mlc.getInteger("attackInterval", 20);
    }

    @Override
    public PathfinderGoal create() {
        return new WrappedZombiesPathfinder(entity, new OptimizedMeleeAttack((EntityCreature)getHandle(),
                speed, attackInterval), getRetargetInterval());
    }

    @Override
    public boolean isValid() {
        return entity.isCreature();
    }
}
