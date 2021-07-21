package io.github.zap.zombies.game.mob.goal.mythicmobs;

import io.github.zap.zombies.game.mob.goal.AttributeValue;
import io.github.zap.zombies.game.mob.goal.BreakWindow;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ai.WrappedPathfindingGoal;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import io.lumine.xikage.mythicmobs.volatilecode.v1_16_R3.ai.PathfinderHolder;
import net.minecraft.server.v1_16_R3.PathfinderGoal;

@MythicAIGoal(
        name = "breakWindow"
)
public class WrappedBreakWindow extends MythicWrapper {
    private final double speed;
    private final int breakTicks;
    private final int breakCount;
    private final double breakReachSquared;

    public WrappedBreakWindow(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);
        speed = mlc.getDouble("speed", 1);
        breakTicks = mlc.getInteger("breakTicks", 20);
        breakCount = mlc.getInteger("breakCount", 1);
        breakReachSquared = mlc.getDouble("breakReachSquared", 9D);
    }

    @Override
    public PathfinderGoal create() {
        return new BreakWindow(entity, new AttributeValue[0], retargetTicks, speed, breakTicks, breakCount, breakReachSquared);
    }

    @Override
    public boolean isValid() {
        return entity.isCreature();
    }
}