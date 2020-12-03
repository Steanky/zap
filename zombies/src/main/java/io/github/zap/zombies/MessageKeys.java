package io.github.zap.zombies;

import lombok.Getter;

public enum MessageKeys {
    GENERIC_ARENA_REJECTION("zombies.arena.reject.generic"),
    NEW_ARENA_REJECTION("zombies.arena.reject.new"),
    OFFLINE_ARENA_REJECTION("zombies.arena.reject.offline"),
    UNKNOWN_ARENA_REJECTION("zombies.arena.reject.unknown");

    @Getter
    private String key;

    MessageKeys(String key) {
        this.key = key;
    }
}
