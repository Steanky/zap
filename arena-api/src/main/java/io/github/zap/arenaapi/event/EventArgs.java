package io.github.zap.arenaapi.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class EventArgs<T extends Event<?>> {
    @Getter
    private T sender;
}
