package io.github.zap.nms.common;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Wrapper<T> {
    protected final T wrapped;

    protected Wrapper(@NotNull T wrapped) {
        Objects.requireNonNull(wrapped, "wrappedObject cannot be null!");
        this.wrapped = wrapped;
    }

    public T getHandle() {
        return wrapped;
    }

    @Override
    public int hashCode() {
        return wrapped.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(wrapped.getClass().isAssignableFrom(obj.getClass())) {
            return obj.equals(wrapped);
        }

        return false;
    }
}
