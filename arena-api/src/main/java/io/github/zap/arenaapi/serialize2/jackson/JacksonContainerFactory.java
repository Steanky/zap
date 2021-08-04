package io.github.zap.arenaapi.serialize2.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zap.arenaapi.serialize2.ContainerFactory;
import io.github.zap.arenaapi.serialize2.DataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

class JacksonContainerFactory implements ContainerFactory {
    private final Logger logger;
    private final ObjectMapper mapper;

    JacksonContainerFactory(@NotNull ObjectMapper mapper, @NotNull Logger logger) {
        this.logger = logger;
        this.mapper = mapper;
    }

    @Override
    public @NotNull Optional<DataContainer> makeFrom(@NotNull Object data) {
        try {
            return Optional.of(new JacksonDataContainer(mapper, mapper.convertValue(data, JsonNode.class)));
        }
        catch (IllegalArgumentException exception) {
            logger.log(Level.WARNING, "Failed to create JacksonDataContainer from object " + data, exception);
        }

        return Optional.empty();
    }
}