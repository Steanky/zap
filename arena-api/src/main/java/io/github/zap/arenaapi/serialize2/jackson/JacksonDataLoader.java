package io.github.zap.arenaapi.serialize2.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zap.arenaapi.serialize2.ContainerFactory;
import io.github.zap.arenaapi.serialize2.DataLoader;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public abstract class JacksonDataLoader implements DataLoader {
    protected final ObjectMapper mapper;
    protected final Logger logger;
    protected final ContainerFactory factory;

    protected JacksonDataLoader(@NotNull ObjectMapper mapper, @NotNull Logger logger) {
        this.mapper = mapper;
        this.logger = logger;
        this.factory = new JacksonContainerFactory(mapper, logger);
    }

    @Override
    public @NotNull ContainerFactory factory() {
        return factory;
    }
}
