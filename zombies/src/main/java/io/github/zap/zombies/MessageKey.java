package io.github.zap.zombies;

import lombok.Getter;

/**
 * Stores resource keys used to generate player translations.
 */
public enum MessageKey {
    MessageKeys("zombies.game.");

    @Getter
    private String key;

    MessageKey(String key) {
        this.key = key;
    }
}
