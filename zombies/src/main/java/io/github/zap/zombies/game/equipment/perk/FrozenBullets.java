package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.zombies.game.data.equipment.perk.FrozenBulletsData;
import io.github.zap.zombies.game.data.equipment.perk.FrozenBulletsLevel;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Reduces the speed of zombies
 */
public class FrozenBullets extends MarkerPerk<@NotNull FrozenBulletsData, @NotNull FrozenBulletsLevel> {

    private double reducedSpeed;

    private int duration;

    public FrozenBullets(@NotNull ZombiesPlayer player, int slot, @NotNull FrozenBulletsData perkData) {
        super(player, slot, perkData);
    }

    public double getReducedSpeed() {
        return reducedSpeed;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public void activate() {
        reducedSpeed = getCurrentLevel().getReducedSpeed();
        duration = getCurrentLevel().getDuration();
    }

    @Override
    public void deactivate() {
        reducedSpeed = 0.0D;
        duration = 0;
    }

}
