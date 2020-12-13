package io.github.zap.arenaapi.event;

public interface EventHandler<T extends Event<T>> {
    void handleEvent(EventArgs<T> arguments);
}
