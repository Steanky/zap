package io.github.zap.arenaapi.serialize2.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zap.arenaapi.serialize2.DataLoader;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class JacksonDataLoaderAbstract implements DataLoader<JacksonDataContainer> {
    protected final ObjectMapper mapper;
    protected final Logger logger;

    protected JacksonDataLoaderAbstract(@NotNull ObjectMapper mapper, @NotNull Logger logger) {
        this.mapper = mapper;
        this.logger = logger;
    }

    @Override
    public @NotNull Optional<JacksonDataContainer> makeContainer(@NotNull Object object) {
        try {
            return Optional.of(new JacksonDataContainer(mapper, mapper.convertValue(object, JsonNode.class)));
        }
        catch (IllegalArgumentException exception) {
            logger.log(Level.WARNING, "Failed to create JacksonDataContainer from object " + object, exception);
        }

        return Optional.empty();
    }
}
