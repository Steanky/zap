package io.github.zap.arenaapi.game;

import org.apache.commons.lang3.Validate;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Metadata {
    private final Map<String, Optional<Object>> mappings = new HashMap<>();

    public Optional<Object> getMetadata(String name) {
        return mappings.getOrDefault(name, Optional.empty());
    }

    public void setMetadata(String name, Object metadata) {
        Validate.notNull(metadata, "metadata cannot be null");
        mappings.put(name, Optional.of(metadata));
    }
}
