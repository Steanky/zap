package io.github.zap.zombies.game.perk;

import io.github.zap.zombies.game.ZombiesPlayer;
import lombok.Getter;

public class FrozenBullets extends MarkerPerk {
    private final double baseSlowdownFactor;

    @Getter
    private final int duration;

    public FrozenBullets(ZombiesPlayer owner, int maxLevel, boolean resetLevelOnDisable, double slowdownFactor, int duration) {
        super(owner, maxLevel, resetLevelOnDisable);
        this.baseSlowdownFactor = slowdownFactor;
        this.duration = duration;
    }

    @Override
    public void activate() {
        //slowdownFactor = baseSlowdownFactor / getCurrentLevel() + 2;
    }

    @Override
    public void deactivate() {
        //slowdownFactor = baseSlowdownFactor;
    }
}
