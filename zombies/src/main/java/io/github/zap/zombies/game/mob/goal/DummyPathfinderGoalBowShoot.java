package io.github.zap.zombies.game.mob.goal;

import net.minecraft.server.v1_16_R3.EntityMonster;
import net.minecraft.server.v1_16_R3.IRangedEntity;
import net.minecraft.server.v1_16_R3.PathfinderGoalBowShoot;

/**
 * PathfinderGoal used to fix skeleton AI. It never starts or does anything at all.
 */
public class DummyPathfinderGoalBowShoot<T extends EntityMonster & IRangedEntity> extends PathfinderGoalBowShoot<T> {
    public DummyPathfinderGoalBowShoot(T self) {
        super(self, 0, 0, 0);
    }

    @Override
    public boolean a() {
        return false;
    }
}
