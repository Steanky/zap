package io.github.zap.zombies.game;

import io.github.zap.arenaapi.event.EmptyEventArgs;

/**
 * Represents a perk that performs no actions.
 */
public abstract class MarkerPerk extends Perk<EmptyEventArgs> {
    public MarkerPerk(ZombiesPlayer owner, int maxLevel) {
        super(owner, null, maxLevel);
    }

    @Override
    public final void execute(EmptyEventArgs args) { }
}
