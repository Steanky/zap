package io.github.zap.arenaapi.event;

import java.util.ArrayList;
import java.util.List;

public abstract class Event<T extends Event<T>> {
    private final List<EventHandler<T>> handlerList = new ArrayList<>();

    public void registerHandler(EventHandler<T> handler) {
        handlerList.add(handler);
    }

    public void removeHandler(EventHandler<T> handler) {
        handlerList.remove(handler);
    }

    protected void callEvent(EventArgs<T> arguments) {
        for(EventHandler<T> handler : handlerList) {
            handler.handleEvent(arguments);
        }
    }
}
