package io.github.zap.zombies.game;

import io.github.zap.arenaapi.event.EmptyEventArgs;

/**
 * Represents a perk that does not involve a recurring event.
 */
public abstract class MarkerPerk extends Perk<EmptyEventArgs> {
    public MarkerPerk(ZombiesPlayer owner, int maxLevel) {
        super(owner, null, maxLevel);
    }

    @Override
    public final void execute(EmptyEventArgs args) { }
}
