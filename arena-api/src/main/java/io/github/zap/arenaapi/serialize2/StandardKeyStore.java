package io.github.zap.arenaapi.serialize2;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class StandardKeyStore implements KeyStore {
    private static final StandardKeyStore GLOBAL = new StandardKeyStore(StringUtils.EMPTY, KeyFactory.standard());

    private final String namespace;
    private final Map<String, DataKey> keys = new HashMap<>();
    private final KeyFactory factory;

    StandardKeyStore(@NotNull String namespace, @NotNull KeyFactory factory) {
        this.namespace = namespace;
        this.factory = factory;
    }

    public static KeyStore global() {
        return GLOBAL;
    }

    @Override
    public @NotNull DataKey named(@NotNull String name) {
        return keys.computeIfAbsent(name, (string) -> factory.make(namespace, name));
    }

    @Override
    public @NotNull Optional<DataKey> existingKey(@NotNull String name) {
        return Optional.ofNullable(keys.get(name));
    }

    @Override
    public @NotNull Set<DataKey> keys() {
        return Set.copyOf(keys.values());
    }

    @Override
    public @NotNull String namespace() {
        return namespace;
    }

    @Override
    public @NotNull KeyFactory keyFactory() {
        return factory;
    }
}
