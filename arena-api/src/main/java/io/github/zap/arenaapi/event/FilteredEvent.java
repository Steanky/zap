package io.github.zap.arenaapi.event;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Filters an event's calls based on a predicate.
 * @param <T> The argument type of the event we are wrapping
 * @param <U> The argument type of this event
 */
public class FilteredEvent<T, U> extends Event<U> {
    public FilteredEvent(Event<T> event, Predicate<T> predicate, Function<T, U> mapper) {
        event.registerHandler((caller, args) -> {
            if(predicate.test(args)) {
                callEvent(mapper.apply(args));
            }
        });
    }
}
