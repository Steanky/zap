package io.github.zap.zombies.game.mob.goal;

import net.minecraft.server.v1_16_R3.EntityCreature;
import net.minecraft.server.v1_16_R3.PathfinderGoalMeleeAttack;

/**
 * Another goal needed to fix skeleton AI. 😿
 */
public class DummyPathfinderGoalMeleeAttack extends PathfinderGoalMeleeAttack {
    public DummyPathfinderGoalMeleeAttack(EntityCreature self) {
        super(self, 0, false);
    }

    @Override
    public boolean a() {
        return false;
    }
}
