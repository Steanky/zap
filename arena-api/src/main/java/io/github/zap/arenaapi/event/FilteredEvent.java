package io.github.zap.arenaapi.event;

import java.util.function.Predicate;

public class FilteredEvent<T> extends Event<T> {
    /**
     * Creates a new FilteredEvent, which will filter calls from the given Event based on the provided predicate.
     * Functionally identical to MappingEvent, except it performs no conversion.
     * @param event The event to filter
     * @param filter The filter itself
     */
    public FilteredEvent(Event<T> event, Predicate<T> filter) {
        event.registerHandler((args) -> {
            if(filter.test(args)) {
                callEvent(args);
            }
        });
    }
}
