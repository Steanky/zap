package io.github.zap.arenaapi.event;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface EventRegister {
    <T> void registerHandler(@NotNull EventHandler<T> eventHandler, @NotNull Class<T> handlerType);
}
