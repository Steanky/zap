package io.github.zap.zombies.game;

import io.github.zap.arenaapi.event.EmptyEventArgs;
import io.github.zap.arenaapi.event.Event;

/**
 * Represents a perk that performs no actions.
 */
public abstract class MarkerPerk extends Perk<EmptyEventArgs> {
    public MarkerPerk(ZombiesPlayer owner, int maxLevel) {
        super(owner, null, maxLevel);
    }

    @Override
    public final void execute(Event<EmptyEventArgs> event, EmptyEventArgs args) { }
}
