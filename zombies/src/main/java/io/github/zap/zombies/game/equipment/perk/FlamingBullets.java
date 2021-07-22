package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.zombies.game.data.equipment.perk.FlamingBulletsData;
import io.github.zap.zombies.game.data.equipment.perk.FlamingBulletsLevel;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Sets zombies on fire
 */
public class FlamingBullets extends MarkerPerk<@NotNull FlamingBulletsData, @NotNull FlamingBulletsLevel> {

    private int duration;

    public FlamingBullets(@NotNull ZombiesPlayer player, int slot,
                          @NotNull FlamingBulletsData perkData) {
        super(player, slot, perkData);
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public void activate() {
        duration = getCurrentLevel().getDuration();
    }

    @Override
    public void deactivate() {
        duration = 0;
    }

}
