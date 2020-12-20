package io.github.zap.arenaapi.event;

/**
 * Encapsulates a function that handles an event.
 * @param <T> The type of object received as an argument
 */
public interface EventHandler<T> {
    /**
     * Handles the actual event.
     * @param caller The Event object that invoked this function
     * @param event The object received as an argument
     */
    void handleEvent(Event<T> caller, T event);
}
