package io.github.zap.zombies.game.mob.goal.mythicmobs;

import io.github.zap.zombies.game.mob.goal.WrappedZombiesPathfinder;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ai.WrappedPathfindingGoal;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import io.lumine.xikage.mythicmobs.volatilecode.v1_16_R3.ai.PathfinderHolder;
import net.minecraft.server.v1_16_R3.EntityCreature;
import net.minecraft.server.v1_16_R3.PathfinderGoal;
import net.minecraft.server.v1_16_R3.PathfinderGoalMeleeAttack;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;

@MythicAIGoal(
        name = "unboundedMeleeAttack"
)
public class WrappedMeleeAttack extends WrappedPathfindingGoal implements PathfinderHolder {
    private final int retargetTicks;

    public WrappedMeleeAttack(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);
        retargetTicks = mlc.getInteger("retargetTicks", -1);
    }

    @Override
    public PathfinderGoal create() {
        return new WrappedZombiesPathfinder(entity, new PathfinderGoalMeleeAttack((EntityCreature)((CraftEntity)
                entity.getBukkitEntity()).getHandle(), 0, false), retargetTicks);
    }

    @Override
    public boolean isValid() {
        return entity.isCreature();
    }
}
