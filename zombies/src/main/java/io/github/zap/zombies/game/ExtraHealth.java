package io.github.zap.zombies.game;

public class ExtraHealth extends MarkerPerk {
    private static final int BASE_HP = 20;

    private final int healthIncrement;

    public ExtraHealth(ZombiesPlayer owner, int maxLevel, int healthIncrement) {
        super(owner, maxLevel);
        this.healthIncrement = healthIncrement;
    }

    @Override
    public void activate() {
        getOwner().getPlayer().setHealth(BASE_HP + getCurrentLevel() * healthIncrement);
    }

    @Override
    public void disable() {
        if(getCurrentLevel() > 0) {
            getOwner().getPlayer().setHealth(BASE_HP);
        }
    }
}
