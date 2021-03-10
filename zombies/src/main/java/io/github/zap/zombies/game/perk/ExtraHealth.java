package io.github.zap.zombies.game.perk;

import io.github.zap.zombies.game.ZombiesPlayer;

public class ExtraHealth extends MarkerPerk {
    private static final int BASE_HP = 20;

    private final int healthIncrement;

    public ExtraHealth(ZombiesPlayer owner, int maxLevel, int healthIncrement, boolean resetLevelOnDisable) {
        super(owner, maxLevel, resetLevelOnDisable);
        this.healthIncrement = healthIncrement;
    }

    @Override
    public void activate() {
        getOwner().getPlayer().setHealth(BASE_HP + getCurrentLevel() * healthIncrement);
    }

    @Override
    public void deactivate() {
        getOwner().getPlayer().setHealth(BASE_HP);
    }
}
