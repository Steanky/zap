package io.github.zap.arenaapi.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class EventArgs<T extends Event<T>> {
    @Getter
    private final T event;
}
