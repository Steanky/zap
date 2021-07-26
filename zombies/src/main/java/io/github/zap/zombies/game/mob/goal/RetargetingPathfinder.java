package io.github.zap.zombies.game.mob.goal;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import net.minecraft.server.v1_16_R3.EntityLiving;

public abstract class RetargetingPathfinder extends BasicMetadataPathfinder {
    private int retargetCounter;

    public RetargetingPathfinder(AbstractEntity entity, AttributeValue[] values, int retargetTicks, double speed, double targetDeviation) {
        super(entity, values, retargetTicks, speed, targetDeviation);
        retargetCounter = self.getRandom().nextInt(retargetTicks);
    }

    @Override
    public void doTick() {
        EntityLiving target = self.getGoalTarget();

        if(target != null) {
            self.getControllerLook().a(target, 30.0F, 30.0F);
        }

        if (++retargetCounter == retargetTicks) {
            //randomly offset the navigation so we don't flood the pathfinder
            this.retargetCounter = self.getRandom().nextInt(retargetTicks / 2);
            retarget();
        }

        if(result != null) {
            setPath(result);
            result = null;
        }
    }

    @Override
    public boolean canStart() {
        return true;
    }

    @Override
    public boolean stayActive() {
        return true;
    }
}
