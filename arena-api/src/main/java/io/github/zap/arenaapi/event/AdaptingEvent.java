package io.github.zap.arenaapi.event;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Filters an event's calls based on a predicate and maps the calls based on a function. The result is a function that
 * will call handlers with the mapped argument. If the predicate fails its test, the event will not be called.
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
