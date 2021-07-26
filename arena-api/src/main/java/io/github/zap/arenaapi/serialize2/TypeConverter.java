package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface TypeConverter<From> {
    record Signature(@NotNull Class<?> from, String namespace) {
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Signature signature) {
                return signature.from.equals(from) && signature.namespace.equals(namespace);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, namespace);
        }
    }

    @NotNull Class<From> convertsFrom();

    @NotNull String namespace();

    @NotNull Object convert(@NotNull From from);
}
