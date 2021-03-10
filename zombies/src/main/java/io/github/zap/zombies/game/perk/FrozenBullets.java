package io.github.zap.zombies.game.perk;

import io.github.zap.zombies.game.ZombiesPlayer;
import lombok.Getter;

public class FrozenBullets extends MarkerPerk {
    private final double baseSlowdownFactor;

    @Getter
    private double slowdownFactor;

    @Getter
    private final int duration;

    public FrozenBullets(ZombiesPlayer owner, int maxLevel, boolean resetLevelOnDisable, double slowdownFactor, int duration) {
        super(owner, maxLevel, resetLevelOnDisable);
        this.baseSlowdownFactor = slowdownFactor;
        this.slowdownFactor = slowdownFactor;
        this.duration = duration;
    }

    @Override
    public void activate() {
        //this is probably bad but shhh, nobody knows it's a rational function unless x > 1
        slowdownFactor = baseSlowdownFactor / getCurrentLevel() == 0 ? 1 : getCurrentLevel();
    }

    @Override
    public void deactivate() {
        slowdownFactor = 1;
    }
}
