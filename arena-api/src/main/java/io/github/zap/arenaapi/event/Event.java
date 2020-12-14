package io.github.zap.arenaapi.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates an event, which is capable of calling a list of EventHandlers.
 * @param <T> The type of the object that will be passed to the EventHandler
 */
public class Event<T> {
    private final List<EventHandler<T>> handlers = new ArrayList<>();

    /**
     * Registers a handler with this event.
     * @param handler The handler to register
     */
    public void registerHandler(EventHandler<T> handler) {
        handlers.add(handler);
    }

    /**
     * Removes a handler from this event.
     * @param handler The handler to remove
     */
    public void removeHandler(EventHandler<T> handler) {
        handlers.remove(handler);
    }

    /**
     * Calls all handlers for this event using the specified arguments.
     * @param args The arguments
     */
    public void callEvent(T args) {
        for(EventHandler<T> handler : handlers) {
            handler.handleEvent(this, args);
        }
    }
}
