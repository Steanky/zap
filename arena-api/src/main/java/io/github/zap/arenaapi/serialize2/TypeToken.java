package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;

/**
 * Contains generic type information.
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

    /**
     * Obtains the type information stored in this object.
     * @return The type information, which may or may not contain additional information about generic types
     */
    public @NotNull Type type() {
        return type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TypeToken typeToken) {
            return typeToken.type.equals(type);
        }

        return false;
    }
}
