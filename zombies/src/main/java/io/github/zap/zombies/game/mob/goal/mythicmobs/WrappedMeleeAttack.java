package io.github.zap.zombies.game.mob.goal.mythicmobs;

import io.github.zap.zombies.game.mob.goal.WrappedZombiesPathfinder;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import net.minecraft.server.v1_16_R3.EntityCreature;
import net.minecraft.server.v1_16_R3.PathfinderGoal;
import net.minecraft.server.v1_16_R3.PathfinderGoalMeleeAttack;

@MythicAIGoal(
        name = "unboundedMeleeAttack"
)
public class WrappedMeleeAttack extends MythicWrapper {
    private final double speed;

    public WrappedMeleeAttack(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);
        speed = mlc.getDouble("speed", 1);
    }

    @Override
    public PathfinderGoal create() {
        return new WrappedZombiesPathfinder(entity, new PathfinderGoalMeleeAttack((EntityCreature)getHandle(),
                speed, false), getRetargetInterval()) {
            @Override
            public boolean b() {
                return true;
            }
        };
    }

    @Override
    public boolean isValid() {
        return entity.isCreature();
    }
}
