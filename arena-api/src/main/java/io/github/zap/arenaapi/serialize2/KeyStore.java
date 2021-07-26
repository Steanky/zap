package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * An object that manages a number of keys under the same namespace. Implementations must at least ensure that keys with
 * duplicate names cannot be created.
 */
public interface KeyStore {
    /**
     * Creates a key with the given name. If the key has already been registered (with a previous call to make() on
     * the same KeyStore) the previously create instance will be returned.
     * @param name The name of the key
     * @return A named key under the namespace managed by this KeyStore
     */
    @NotNull DataKey named(@NotNull String name);

    /**
     * Gets a key that is stored in this KeyStore, using only the key's name.
     * @param name The name of the stored key
     * @return An Optional object, whose data is present if the key's name exists and is not mapped to null
     */
    @NotNull Optional<DataKey> existingKey(@NotNull String name);

    /**
     * Returns a copy of all of the DataKey objects managed by this KeyStore.
     * @return A set of managed keys
     */
    @NotNull Set<DataKey> keys();

    /**
     * Returns the namespace of this KeyStore. KeyStore implementations are required to have a "globally unique"
     * namespace.
     */
    @NotNull String namespace();

    /**
     * Gets the factory used to create and validate keys from strings.
     */
    @NotNull KeyFactory keyFactory();

    /**
     * Creates a new KeyStore with the given namespace and KeyFactory, which will be used to create new keys.
     * @param namespace The namespace
     * @param factory The factory
     * @return A new KeyStore object
     */
    static @NotNull KeyStore from(@NotNull String namespace, @NotNull KeyFactory factory) {
        return new StandardKeyStore(namespace, factory);
    }

    /**
     * Creates a new KeyStore with the given namespace.
     * @param namespace The namespace of the desired KeyStore
     * @return A new KeyStore object
     */
    static @NotNull KeyStore from(@NotNull String namespace) {
        return from(namespace, KeyFactory.standard());
    }
}
