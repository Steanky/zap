package io.github.zap.zombies.game.mob.goal;

import io.github.zap.zombies.Zombies;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;

public abstract class StandardMetadataPathfinder extends ZombiesPathfinder {
    public StandardMetadataPathfinder(AbstractEntity entity) {
        super(entity, Zombies.ARENA_METADATA_NAME, Zombies.WINDOW_METADATA_NAME);
    }
}
