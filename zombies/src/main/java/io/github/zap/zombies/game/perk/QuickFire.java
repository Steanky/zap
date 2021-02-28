package io.github.zap.zombies.game.perk;

import io.github.zap.zombies.game.ZombiesPlayer;

public class QuickFire extends MarkerPerk {
    public QuickFire(ZombiesPlayer owner, int maxLevel, boolean resetOnQuit) {
        super(owner, maxLevel, resetOnQuit);
    }
    private int lastLevel = 0;

    @Override
    public void activate() {
        super.activate();
        getOwner().setFireRateMultiplier(getOwner().getFireRateMultiplier() / getMultiplier(lastLevel) * getMultiplier(getCurrentLevel()));
    }

    @Override
    public boolean upgrade() {
        lastLevel = getCurrentLevel();
        return super.upgrade();
    }

    @Override
    public boolean downgrade() {
        lastLevel = getCurrentLevel();
        return super.downgrade();
    }

    private double getMultiplier(int level) {
        return 1 - 0.25d * level;
    }
}
