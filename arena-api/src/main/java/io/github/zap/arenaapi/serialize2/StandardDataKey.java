package io.github.zap.arenaapi.serialize2;

import io.github.zap.party.shadow.io.github.regularcommands.util.StringUtils;
import org.jetbrains.annotations.NotNull;

class StandardDataKey implements DataKey {
    private final String key;
    private final int delimiterIndex;

    StandardDataKey(@NotNull String key, int delimiterIndex) {
        this.key = key;
        this.delimiterIndex = delimiterIndex;
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
