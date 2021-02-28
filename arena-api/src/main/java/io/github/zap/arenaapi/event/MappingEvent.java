package io.github.zap.arenaapi.event;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Filters an event's calls based on a predicate and maps the calls based on a function. The result is a function that
 * will call handlers with the mapped argument. If the predicate fails its test, the event will not be called.
 * @param <T> The argument type of the event we are wrapping
 * @param <U> The argument type of this event
 */
public class MappingEvent<T, U> extends Event<U> {
    public MappingEvent(Event<T> event, MappingPredicate<T, U> mapper) {
        event.registerHandler((args) -> {
            Pair<Boolean, U> result = mapper.tryMap(args);
            if(result.getLeft()) {
                callEvent(result.getRight());
            }
        });
    }
}
