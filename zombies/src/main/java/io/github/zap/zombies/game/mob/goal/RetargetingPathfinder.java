package io.github.zap.zombies.game.mob.goal;

import io.github.zap.arenaapi.pathfind.PathResult;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;

public abstract class RetargetingPathfinder extends BasicMetadataPathfinder {
    public RetargetingPathfinder(AbstractEntity entity, AttributeValue[] values, double speed, int targetDeviation) {
        super(entity, values, speed, targetDeviation);
    }

    @Override
    public boolean canStart() {
        if(zombiesPlayer == null) {
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
