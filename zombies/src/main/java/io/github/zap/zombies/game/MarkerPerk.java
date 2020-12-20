package io.github.zap.zombies.game;

import io.github.zap.arenaapi.event.EmptyEventArgs;
import io.github.zap.arenaapi.event.Event;

/**
 * Represents a perk that performs no actions. Rather, its 'active' state is queried by the ZombiesPlayer at certain
 * times.
 */
public abstract class MarkerPerk extends Perk<EmptyEventArgs> {
    public MarkerPerk(ZombiesPlayer owner) {
        super(owner, null);
    }

    @Override
    public final void execute(Event<EmptyEventArgs> event, EmptyEventArgs args) { }
}
