package io.github.zap.localization;

import lombok.Getter;

public enum MessageKey {
    EXAMPLE_KEY("example.key"),
    LOCALE("locale");

    @Getter
    private final String resourceKey;

    MessageKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
