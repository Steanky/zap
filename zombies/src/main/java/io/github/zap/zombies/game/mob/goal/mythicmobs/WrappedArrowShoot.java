package io.github.zap.zombies.game.mob.goal.mythicmobs;

import io.github.zap.zombies.game.mob.goal.WrappedZombiesPathfinder;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import net.minecraft.server.v1_16_R3.*;

@MythicAIGoal(
        name = "unboundedArrowAttack"
)
public class WrappedArrowShoot extends MythicWrapper {
    private final double speed;
    private final int fireInterval;
    private final float targetDistance;

    public WrappedArrowShoot(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);
        speed = mlc.getDouble("speed", 1);
        fireInterval = mlc.getInteger("fireInterval", 20);
        targetDistance = mlc.getFloat("targetDistance", 1);
    }

    @Override
    public PathfinderGoal create() {
        return new WrappedZombiesPathfinder(entity, new PathfinderGoalArrowAttack((IRangedEntity) getHandle(),
                speed, fireInterval, targetDistance), getRetargetInterval());
    }

    @Override
    public boolean isValid() {
        return getHandle() instanceof IRangedEntity;
    }
}
