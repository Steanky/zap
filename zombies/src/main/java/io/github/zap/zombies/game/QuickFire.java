package io.github.zap.zombies.game;

public class QuickFire extends MarkerPerk {
    private final int baseDelayReduction;

    public QuickFire(ZombiesPlayer owner, int maxLevel, int delayReduction) {
        super(owner, maxLevel);
        this.baseDelayReduction = delayReduction;
    }

    public int getDelayReduction() {
        return baseDelayReduction * getCurrentLevel();
    }
}
