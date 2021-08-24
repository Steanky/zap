package io.github.zap.arenaapi.event;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class FilteredEvent<T> extends Event<T> {
    private final Event<T> underlyingEvent;

    /**
     * Creates a new FilteredEvent, which will filter calls from the given Event based on the provided predicate.
     * Functionally identical to MappingEvent, except it performs no conversion.
     * @param event The event to filter
     * @param filter The filter itself
     */
    public FilteredEvent(@NotNull Event<T> event, @NotNull Predicate<T> filter) {
        this.underlyingEvent = event;

        event.registerHandler((args) -> {
            if(filter.test(args)) {
                callEvent(args);
            }
        });
    }

    @Override
    public void dispose() {
        if(disposed) {
            return;
        }

        super.dispose();
        underlyingEvent.dispose();
    }
}
