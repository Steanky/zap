package io.github.zap.arenaapi.event;

import lombok.Getter;
import lombok.Setter;

/**
 * Encapsulates an event that can be cancelled.
 */
public abstract class CancellableEvent extends CustomEvent {
    @Getter
    @Setter
    public boolean cancelled;

    /**
     * Creates a new CancellableEvent.
     * @param initialState Whether or not the event is initially flagged as cancellable
     */
    public CancellableEvent(boolean initialState) {
        cancelled = initialState;
    }
}
