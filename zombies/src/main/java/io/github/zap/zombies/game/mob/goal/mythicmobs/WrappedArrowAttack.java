package io.github.zap.zombies.game.mob.goal.mythicmobs;

import io.github.zap.zombies.game.mob.goal.WrappedZombiesPathfinder;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ai.WrappedPathfindingGoal;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import io.lumine.xikage.mythicmobs.volatilecode.v1_16_R3.ai.PathfinderHolder;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;

@MythicAIGoal(
        name = "unboundedArrowAttack"
)
public class WrappedArrowAttack extends MythicWrapper {
    private final double speed;
    private final int fireInterval;
    private final float targetDistance;

    public WrappedArrowAttack(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);
        speed = mlc.getDouble("speed", 1);
        fireInterval = mlc.getInteger("fireInterval", 40);
        targetDistance = mlc.getFloat("targetDistance", 10);
    }

    @Override
    public PathfinderGoal create() {
        return new WrappedZombiesPathfinder(entity, new PathfinderGoalArrowAttack((IRangedEntity) getHandle(), speed, fireInterval, targetDistance), getRetargetInterval());
    }

    @Override
    public boolean isValid() {
        return getHandle() instanceof IRangedEntity;
    }
}
