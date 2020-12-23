package io.github.zap.zombies.game.perk;

import io.github.zap.arenaapi.event.EmptyEventArgs;
import io.github.zap.zombies.game.ZombiesPlayer;

/**
 * Represents a perk that does not involve a recurring event.
 */
public abstract class MarkerPerk extends Perk<EmptyEventArgs> {
    public MarkerPerk(ZombiesPlayer owner, int maxLevel, boolean resetLevelOnDisable) {
        super(owner, null, maxLevel, resetLevelOnDisable);
    }

    @Override
    public final void execute(EmptyEventArgs args) { }
}
