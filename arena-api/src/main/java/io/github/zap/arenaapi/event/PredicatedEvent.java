package io.github.zap.arenaapi.event;

import java.util.function.Predicate;

/**
 * Event that tests a condition before calling its listeners.
 * @param <T> The type of object the event receives
 */
public class PredicatedEvent<T> extends Event<T> {
    private final Predicate<T> predicate;

    public PredicatedEvent(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public void callEvent(T args) {
        if(predicate.test(args)) {
            super.callEvent(args);
        }
    }
}
