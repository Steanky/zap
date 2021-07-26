package io.github.zap.arenaapi.serialize2;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

class StandardKeyStore implements KeyStore {
    private static final Set<String> NAMESPACES = new HashSet<>();

    private final String namespace;
    private final Set<DataKey> keys = new HashSet<>();

    StandardKeyStore(@NotNull String namespace) {
        if(NAMESPACES.add(namespace)) {
            this.namespace = namespace;
        }
        else {
            throw new IllegalArgumentException("Namespace '" + namespace + "' already has a registered store");
        }
    }

    @Override
    public @NotNull DataKey make(@NotNull String name) {
        StandardDataKey key = new StandardDataKey(namespace, name);
        if(keys.add(key)) {
            return new StandardDataKey(namespace, name);
        }

        throw new IllegalArgumentException("A key with name '" + name + "' already exists in this store");
    }

    @Override
    public boolean hasKeyWithName(@NotNull String name) {
        for(DataKey key : keys) {
            if(key.name().equals(name)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public @NotNull DataKey getKeyWithName(@NotNull String name) {
        for(DataKey key : keys) {
            if(key.name().equals(name)) {
                return key;
            }
        }

        throw new IllegalArgumentException("Key with name '" + name + "' does not exist in this store");
    }

    @Override
    public @NotNull Set<DataKey> keys() {
        return ImmutableSet.copyOf(keys);
    }

    @Override
    public @NotNull String namespace() {
        return namespace;
    }
}
