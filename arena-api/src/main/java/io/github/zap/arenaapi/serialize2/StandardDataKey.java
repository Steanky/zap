package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

class StandardDataKey implements DataKey {
    private static final String DELIMITER = ":";

    private final String key;
    private final int delimiterIndex;

    StandardDataKey(@NotNull String namespace, @NotNull String name) {
        if(namespace.isEmpty() || name.isEmpty()) {
            throw new IllegalArgumentException("Namespace and name must both be non-empty strings");
        }

        if(namespace.contains(DELIMITER) || name.contains(DELIMITER)) {
            throw new IllegalArgumentException("Data namespace and name must not contain delimiter string '" +
                    DELIMITER + "'");
        }

        key = namespace.concat(DELIMITER).concat(name);
        delimiterIndex = namespace.length();
    }

    @Override
    public @NotNull String namespace() {
        return key.substring(0, delimiterIndex);
    }

    @Override
    public @NotNull String name() {
        return key.substring(delimiterIndex + 1);
    }

    @Override
    public @NotNull String key() {
        return key;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof DataKey key) {
            return this.key.equals(key.key());
        }

        return false;
    }
}
