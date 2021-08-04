package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;

/**
 * Represents a reference to a generic type.
 */
public abstract class TypeToken<T> implements Comparable<TypeToken<T>> {
    private final Type type;

    public TypeToken() {
        type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected TypeToken(@NotNull Type type) {
        this.type = type;
    }

    @Override
    public int compareTo(@NotNull TypeToken<T> o) {
        return 0;
    }

    public @NotNull Type type() {
        return type;
    }
}
