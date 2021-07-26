package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

public interface KeyFactory {
    /**
     * Returns true if the given "raw" string is a valid key. Must contain both namespace and name information.
     * @param raw The raw string to validate
     * @return True if the string is valid; false otherwise
     */
    boolean validKeySyntax(@NotNull String raw);

    /**
     * Makes a key, using the specific namespace and name information. If either namespace or name are invalid, an
     * exception will be thrown.
     * @param namespace The namespace to use
     * @param name The name to use
     * @return A new DataKey implementation containing namespace and name information
     */
    @NotNull DataKey make(@NotNull String namespace, @NotNull String name);

    /**
     * Makes a key using a raw input string. If the string's syntax is invalid, an exception will be thrown.
     * @param raw The string to convert into a key
     * @return The key, if the syntax is valid
     */
    @NotNull DataKey makeRaw(@NotNull String raw);
}
