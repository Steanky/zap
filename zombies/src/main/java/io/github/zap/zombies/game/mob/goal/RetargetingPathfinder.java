package io.github.zap.zombies.game.mob.goal;

import io.github.zap.arenaapi.pathfind.PathResult;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import net.minecraft.server.v1_16_R3.EntityLiving;

public abstract class RetargetingPathfinder extends BasicMetadataPathfinder {
    private PathResult result;
    private int navigationCounter;

    public RetargetingPathfinder(AbstractEntity entity, AttributeValue[] values, double speed, double targetDeviation) {
        super(entity, values, speed, targetDeviation);
    }

    @Override
    public void doTick() {
        EntityLiving target = self.getGoalTarget();

        if(target != null) {
            self.getControllerLook().a(target, 30.0F, 30.0F);
            this.navigationCounter = Math.max(this.navigationCounter - 1, 0);

            if (this.navigationCounter <= 0) {
                //randomly offset the navigation so we don't flood the pathfinder
                this.navigationCounter = 4 + self.getRandom().nextInt(10);
                result = retarget();
            }

            if(result != null) {
                setPath(result);
                result = null;
            }
        }
    }

    @Override
    public boolean canStart() {
        if(zombiesPlayer == null || !zombiesPlayer.isAlive()) {
            PathResult path = retarget();

            if(path != null) {
                setPath(path);
                return true;
            }
            else {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean stayActive() {
        return zombiesPlayer != null && zombiesPlayer.isAlive();
    }
}
