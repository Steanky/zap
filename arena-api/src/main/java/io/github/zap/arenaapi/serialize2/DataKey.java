package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

/**
 * Encapsulates an object used as a key to access a DataContainer. At its core is a simple String object, although
 * every DataKey can be considered to have a "namespace" and a "name". This mimics Minecraft's identifiers.
 *
 * DataKey objects should be singletons in general. This usage may be enforced via use of a KeyStore, which additionally
 * must prevent equal DataKey instances from being made.
 */
public interface DataKey {
    /**
     * Returns a string representing the namespace of the DataKey.
     * @return The namespace of the DataKey
     */
    @NotNull String namespace();

    /**
     * Returns a string representing the name of the DataKey.
     * @return The name of the DataKey
     */
    @NotNull String name();

    /**
     * Returns a string which is a combination of the namespace and name, which may be used as a unique identifier even
     * in situations in which DataContainers are being stored alongside keys of other namespaces. A typical pattern
     * mimics Minecraft's identifiers, ex. namespace:name, but this is not enforced and other formats may be used.
     * @return A combination of namespace and name
     */
    @NotNull String key();
}
