package io.github.zap.zombies.game;

import lombok.Getter;

public class QuickFire extends MarkerPerk {
    @Getter
    private final int delayReduction;

    public QuickFire(ZombiesPlayer owner, int delayReduction) {
        super(owner);
        this.delayReduction = delayReduction;
    }
}
