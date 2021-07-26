package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * An object that manages a number of keys under the same namespace. Implementations must ensure that keys with
 * duplicate names cannot be created.
 */
public interface KeyStore {
    /**
     * Creates a key with the given name. If the key has already been registered (with a previous call to make() on
     * the same KeyStore) an exception will be thrown.
     * @param name The name of the key
     * @return A named key under the namespace managed by this KeyStore
     */
    @NotNull DataKey make(@NotNull String name);

    /**
     * Returns whether or not a key with this name exists in this KeyStore.
     * @param name The name to check
     * @return true if a key with the provided name exists in this store, false otherwise
     */
    boolean hasKeyWithName(@NotNull String name);

    /**
     * Gets an existing key from this KeyStore.
     *
     * This method exists primarily for potential compatibility concerns and should generally not be used unless no
     * accessible source of keys used for a particular namespace exists.
     *
     * Will throw an exception if no key with the given name exists
     * @param name The name of the key to retrieve
     * @return A stored DataKey object
     */
    @NotNull DataKey getKeyWithName(@NotNull String name);

    /**
     * Returns an immutable copy of all of the DataKey objects managed by this KeyStore.
     * @return A set of managed keys
     */
    @NotNull Set<DataKey> keys();

    /**
     * Returns the namespace of this KeyStore. KeyStore implementations are required to have a "globally unique"
     * namespace, and can be expected to enforce this using a static variable.
     */
    @NotNull String namespace();

    /**
     * Creates a new KeyStore with the given namespace. Multiple instances of KeyStore must possess unique namespaces.
     * @param namespace The namespace of the desired KeyStore
     * @return A new KeyStore object
     */
    static @NotNull KeyStore ofNamespace(@NotNull String namespace) {
        return new StandardKeyStore(namespace);
    }
}
