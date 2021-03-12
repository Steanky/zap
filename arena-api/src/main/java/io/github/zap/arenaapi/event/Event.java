package io.github.zap.arenaapi.event;

import io.github.zap.arenaapi.Disposable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Encapsulates an event, which is capable of calling a list of EventHandlers. EVENTS ARE NOT THREAD SAFE! VERY VERY bad
 * things will happen if more than one thread tries to access an event
 * @param <T> The type of the object that will be passed to the EventHandler
 */
public class Event<T> implements Disposable {
    private final List<EventHandler<T>> handlers = new ArrayList<>();

    private final Queue<EventHandler<T>> pendingAdditions = new ArrayDeque<>();
    private final Queue<EventHandler<T>> pendingDeletions = new ArrayDeque<>();

    private boolean clearPending = false;

    private boolean invokingHandlers = false;

    /**
     * Returns the number of handlers registered.
     * @return The number of handlers registered
     */
    public int handlerCount() {
        return handlers.size();
    }

    /**
     * Registers a handler with this event. If handlers are currently in the process of being called, the specified
     * handler will be removed only after all the handlers have been called once.
     * @param handler The handler to register
     */
    public void registerHandler(EventHandler<T> handler) {
        if(invokingHandlers) {
            pendingAdditions.add(handler);
        }
        else {
            handlers.add(handler);
        }
    }

    /**
     * Removes a handler from this event. If handlers are currently in the process of being called, the specified
     * handler will be removed only after all the handlers have been called once.
     * @param handler The handler to remove
     */
    public void removeHandler(EventHandler<T> handler) {
        if(invokingHandlers) {
            pendingDeletions.add(handler);
        }
        else {
            handlers.remove(handler);
        }
    }

    /**
     * Removes all handlers from this Event. If handlers are currently in the process of being called, all handlers
     * will clear after they finish being called.
     */
    public void clearHandlers() {
        if(invokingHandlers) {
            clearPending = true;
        }
        else {
            handlers.clear();
        }
    }

    @Override
    public void dispose() {
        if(invokingHandlers) {
            clearPending = true;
        }
        else {
            handlers.clear();
            pendingAdditions.clear();
            pendingDeletions.clear();
        }
    }

    /**
     * Calls all handlers for this event using the specified arguments.
     * @param args The arguments
     */
    public void callEvent(T args) {
        invokingHandlers = true;
        for(EventHandler<T> handler : handlers) {
            handler.handleEvent(args);
        }

        if(clearPending) {
            handlers.clear();
        }
        else {
            while(pendingAdditions.size() > 0) {
                handlers.add(pendingAdditions.remove());
            }

            while(pendingDeletions.size() > 0) {
                handlers.remove(pendingDeletions.remove());
            }
        }
        invokingHandlers = false;
    }
}
