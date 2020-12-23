package io.github.zap.arenaapi.event;

/**
 * Encapsulates a function that handles an event.
 * @param <T> The type of object received as an argument
 */
public interface EventHandler<T> {
    /**
     * Handles the actual event.
     * @param args The object received as an argument
     */
    void handleEvent(T args);
}
