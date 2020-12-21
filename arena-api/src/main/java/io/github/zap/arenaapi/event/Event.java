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
     * Returns the number of handlers registered.
     * @return The number of handlers registered
     */
    public int handlerCount() {
        return handlers.size();
    }

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
     * Performs cleanup tasks.
     */
    public void close() {
        handlers.clear();
    }

    /**
     * Calls all handlers for this event using the specified arguments.
     * @param args The arguments
     */
    public void callEvent(T args) {
        for(EventHandler<T> handler : handlers) {
            handler.handleEvent(args);
        }
    }
}
