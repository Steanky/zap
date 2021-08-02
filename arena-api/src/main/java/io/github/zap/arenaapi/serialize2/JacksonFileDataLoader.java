package io.github.zap.arenaapi.serialize2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JacksonFileDataLoader implements DataLoader {
    private final Logger logger;
    private final DataMarshal marshal;
    private final ObjectMapper mapper;
    private final File file;

    private final ObjectWriter writer;

    public JacksonFileDataLoader(@NotNull Logger logger, @NotNull DataMarshal marshal, @NotNull ObjectMapper mapper, @NotNull File file) {
        this.logger = logger;
        this.marshal = marshal;
        this.mapper = mapper;
        this.file = file;

        this.writer = mapper.writer();
    }

    @Override
    public @NotNull Optional<DataContainer> read() {
        try {
            return Optional.of(marshal.fromMappings(mapper.convertValue(mapper.readTree(file), new TypeReference<>() {})));
        } catch (IOException error) {
            logger.log(Level.WARNING, "IOException occurred while parsing JSON data from file " + file + " using Jackson:");
            logger.log(Level.WARNING, error.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public void write(@NotNull DataContainer container) {
        Map<String, Object> mappings = marshal.toMappings(container);

        try {
            writer.writeValue(file, mappings);
        } catch (IOException error) {
            logger.log(Level.WARNING, "IOException occurred while writing JSON data to file " + file + " using Jackson:");
            logger.log(Level.WARNING, error.getMessage());
        }
    }
}
