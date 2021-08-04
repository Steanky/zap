package io.github.zap.arenaapi.serialize2.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zap.arenaapi.serialize2.DataLoader;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public abstract class JacksonDataLoader implements DataLoader {
    protected final ObjectMapper mapper;
    protected final Logger logger;
    protected final JacksonContainerFactory factory;

    protected JacksonDataLoader(@NotNull ObjectMapper mapper, @NotNull Logger logger,
                                @NotNull JacksonContainerFactory factory) {
        this.mapper = mapper;
        this.logger = logger;
        this.factory = factory;
    }

    @Override
    public @NotNull JacksonContainerFactory factory() {
        return factory;
    }
}
