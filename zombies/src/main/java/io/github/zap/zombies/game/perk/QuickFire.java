package io.github.zap.zombies.game.perk;

import io.github.zap.arenaapi.ObjectDisposedException;
import io.github.zap.zombies.game.ZombiesPlayer;

public class QuickFire extends MarkerPerk {
    private static final String MODIFIER_NAME = "quick_fire";

    public QuickFire(ZombiesPlayer owner, int maxLevel, boolean resetOnQuit) {
        super(owner, maxLevel, resetOnQuit);
    }
    private int lastLevel = 0;

    @Override
    public void activate() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        getOwner().getFireRateMultiplier().registerModifier(MODIFIER_NAME, d -> d == null ? 1D : d / getMultiplier(lastLevel) * getMultiplier(getCurrentLevel()));
    }

    @Override
    public void deactivate() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        getOwner().getFireRateMultiplier().removeModifier(MODIFIER_NAME);
    }

    @Override
    public boolean upgrade() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        lastLevel = getCurrentLevel();
        return super.upgrade();
    }

    @Override
    public boolean downgrade() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        lastLevel = getCurrentLevel();
        return super.downgrade();
    }

    private double getMultiplier(int level) {
        return 1 - 0.25d * level;
    }
}
