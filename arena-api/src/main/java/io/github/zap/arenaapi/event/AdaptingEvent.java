package io.github.zap.arenaapi.event;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Filters an event's calls based on a predicate and maps the calls based on a function.
 * @param <T> The argument type of the event we are wrapping
 * @param <U> The argument type of this event
 */
public class AdaptingEvent<T, U> extends Event<U> {
    public AdaptingEvent(Event<T> event, Predicate<T> predicate, Function<T, U> mapper) {
        event.registerHandler((args) -> {
            if(predicate.test(args)) {
                callEvent(mapper.apply(args));
            }
        });
    }
}
