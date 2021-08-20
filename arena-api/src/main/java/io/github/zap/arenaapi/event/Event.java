package io.github.zap.arenaapi.event;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.ObjectDisposedException;

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

    private final boolean rethrowExceptions;

    private boolean clearPending = false;
    private boolean invokingHandlers = false;

    protected boolean disposed = false;

    /**
     * Creates a new event with the specified exception handling policy.
     * @param rethrowExceptions The exception handling policy, which if true will rethrow exceptions outside of the
     *                          calling loop (that is, one handler cannot prevent the execution of other handlers if
     *                          it throws an exception).
     */
    public Event(boolean rethrowExceptions) {
        this.rethrowExceptions = rethrowExceptions;
    }

    public Event() {
        this(true);
    }

    /**
     * Returns the number of handlers registered.
     * @return The number of handlers registered
     */
    public int handlerCount() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        return handlers.size();
    }

    /**
     * Registers a handler with this event. If handlers are currently in the process of being called, the specified
     * handler will be added only after all the handlers have been called once.
     * @param handler The handler to register
     */
    public void registerHandler(EventHandler<T> handler) {
        if(disposed) {
            throw new ObjectDisposedException();
        }

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
        if(disposed) {
            throw new ObjectDisposedException();
        }

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
        if(disposed) {
            throw new ObjectDisposedException();
        }

        if(invokingHandlers) {
            clearPending = true;
        }
        else {
            handlers.clear();
        }
    }

    @Override
    public void dispose() {
        if(disposed) {
            return;
        }

        if(invokingHandlers) {
            clearPending = true;
        }
        else {
            handlers.clear();
            pendingAdditions.clear();
            pendingDeletions.clear();
        }

        disposed = true;
    }

    /**
     * Calls all handlers for this event using the specified arguments. If a registered EventHandler throws a
     * RuntimeException and this Event is configured to rethrow exceptions, any remaining handlers will continue to be
     * called. If it is not configured to rethrow exceptions, any remaining handlers will not be called.
     * @param args The arguments
     */
    public void callEvent(T args) {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        invokingHandlers = true;
        try {
            if(rethrowExceptions) {
                callWithRethrow(args);
            }
            else {
                callWithoutRethrow(args);
            }
        }
        finally {
            invokingHandlers = false;

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
        }
    }

    private void callWithRethrow(T args) {
        RuntimeException rethrow = null;
        for(EventHandler<T> handler : handlers) {
            try {
                handler.handleEvent(args);
            }
            catch (RuntimeException exception) {
                rethrow = exception;
            }
        }

        if(rethrow != null) {
            throw rethrow;
        }
    }

    private void callWithoutRethrow(T args) {
        for(EventHandler<T> handler : handlers) {
            handler.handleEvent(args);
        }
    }
}
