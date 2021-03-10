package io.github.zap.zombies.game.perk;

import io.github.zap.zombies.game.ZombiesPlayer;
import lombok.Getter;

public class FastRevive extends MarkerPerk {
    private final int defaultReviveTime;

    private final int tickReductionPerLevel;

    @Getter
    private int reviveTime;

    public FastRevive(ZombiesPlayer owner, int maxLevel, boolean resetOnQuit, int defaultReviveTime,
                      int tickReductionPerLevel) {
        super(owner, maxLevel, resetOnQuit);
        this.defaultReviveTime = defaultReviveTime;
        this.reviveTime = defaultReviveTime;
        this.tickReductionPerLevel = tickReductionPerLevel;
    }

    @Override
    public void activate() {
        reviveTime = defaultReviveTime - tickReductionPerLevel * getCurrentLevel();
    }

    @Override
    public void deactivate() {
        reviveTime = defaultReviveTime;
    }
}
