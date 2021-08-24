package io.github.zap.arenaapi.event;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

/**
 * Filters an event's calls based on the results of a MappingPredicate, which performs a validation and a conversion
 * on the given input arguments.
 * @param <T> The argument type of the event we are wrapping
 * @param <U> The argument type of this event
 */
public class MappingEvent<T, U> extends Event<U> {
    private final Event<T> underlyingEvent;

    /**
     * Creates a new MappingEvent attached to the provided Event, to which a handler is registered. When the given
     * Event is invoked, this MappingEvent will attempt to perform a validation and mapping conversion before calling
     * its own handlers with the resulting value of the conversion. It will not call anything if the MappingPredicate
     * fails.
     * @param event The event to register to
     * @param mapper The mapper to test and convert event arguments
     */
    public MappingEvent(@NotNull Event<T> event, @NotNull MappingPredicate<T, U> mapper) {
        underlyingEvent = event;

        event.registerHandler((args) -> {
            Pair<Boolean, U> result = mapper.tryMap(args);
            if(result.getLeft()) {
                callEvent(result.getRight());
            }
        });
    }

    @Override
    public void dispose() {
        if(super.disposed) {
            return;
        }

        super.dispose();
        underlyingEvent.dispose();
    }
}
