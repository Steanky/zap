package io.github.zap.zombies;

import lombok.Getter;

public enum MessageKeys {
    /*
    These are all error messages that should be displayed to the player when they fail to join an arena.
     */
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
