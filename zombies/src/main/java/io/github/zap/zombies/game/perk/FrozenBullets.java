package io.github.zap.zombies.game.perk;

import io.github.zap.zombies.game.ZombiesPlayer;
import lombok.Getter;

public class FrozenBullets extends MarkerPerk {
    @Getter
    private double slowdownFactor;

    public FrozenBullets(ZombiesPlayer owner, int maxLevel, boolean resetLevelOnDisable, double slowdownFactor) {
        super(owner, maxLevel, resetLevelOnDisable);
        this.slowdownFactor = slowdownFactor;
    }

    @Override
    public void activate() {
        slowdownFactor = getCurrentLevel();
    }

    @Override
    public void deactivate() {
        slowdownFactor = 0;
    }
}
