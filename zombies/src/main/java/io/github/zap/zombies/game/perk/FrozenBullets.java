package io.github.zap.zombies.game.perk;

import io.github.zap.arenaapi.ObjectDisposedException;
import io.github.zap.zombies.game.ZombiesPlayer;
import lombok.Getter;

public class FrozenBullets extends MarkerPerk {
    private final double baseSlowdownFactor;

    @Getter
    private final int duration;

    @Getter
    private double slowdownFactor;

    public FrozenBullets(ZombiesPlayer owner, int maxLevel, boolean resetLevelOnDisable, double slowdownFactor, int duration) {
        super(owner, maxLevel, resetLevelOnDisable);
        this.baseSlowdownFactor = slowdownFactor;
        this.duration = duration;
    }

    @Override
    public void activate() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        slowdownFactor = baseSlowdownFactor / getCurrentLevel() + 1;
    }

    @Override
    public void deactivate() {
        slowdownFactor = baseSlowdownFactor;
    }
}
