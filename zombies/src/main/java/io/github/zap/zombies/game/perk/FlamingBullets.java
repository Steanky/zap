package io.github.zap.zombies.game.perk;

import io.github.zap.arenaapi.ObjectDisposedException;
import io.github.zap.zombies.game.ZombiesPlayer;
import lombok.Getter;

public class FlamingBullets extends MarkerPerk {
    private final int baseDuration;

    @Getter
    private int duration;

    public FlamingBullets(ZombiesPlayer owner, int maxLevel, boolean resetLevelOnDisable, int duration) {
        super(owner, maxLevel, resetLevelOnDisable);
        this.baseDuration = duration;
        this.duration = 0;
    }

    @Override
    public void activate() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        //linear scaling :shrug:
        duration = baseDuration * getCurrentLevel();
    }

    @Override
    public void deactivate() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        duration = 0;
    }
}
