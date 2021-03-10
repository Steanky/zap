package io.github.zap.zombies.game.perk;

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
        //linear scaling :shrug:
        duration = baseDuration * getCurrentLevel();
    }

    @Override
    public void deactivate() {
        duration = 0;
    }
}
