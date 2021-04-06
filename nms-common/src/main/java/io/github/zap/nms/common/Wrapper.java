package io.github.zap.nms.common;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Wrapper<T> {
    protected final T wrappedObject;

    protected Wrapper(@NotNull T wrappedObject) {
        Objects.requireNonNull(wrappedObject, "wrappedObject cannot be null!");
        this.wrappedObject = wrappedObject;
    }

    public T getHandle() {
        return wrappedObject;
    }
}
