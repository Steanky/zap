package io.github.zap.arenaapi.serialize2.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.github.zap.arenaapi.serialize2.DataContainer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JacksonFileDataLoader extends JacksonDataLoader {
    private final Logger logger;
    private final File file;

    private final ObjectWriter writer;

    public JacksonFileDataLoader(@NotNull ObjectMapper mapper, @NotNull Logger logger, @NotNull File file) {
        super(mapper, logger);
        this.logger = logger;
        this.file = file;

        this.writer = mapper.writer();
    }

    @Override
    public @NotNull Optional<DataContainer> read() {
        try {
            return Optional.of(new JacksonDataContainer(mapper, mapper.readTree(file)));
        } catch (IOException error) {
            logger.log(Level.WARNING, "IOException occurred while parsing JSON data from file " + file + " using Jackson", error);
        }

        return Optional.empty();
    }

    @Override
    public void write(@NotNull DataContainer container) {
        if(container instanceof JacksonDataContainer jacksonDataContainer) {
            try {
                writer.writeValue(file, jacksonDataContainer.node());
            } catch (IOException error) {
                logger.log(Level.WARNING, "IOException occurred while writing JSON data to file " + file + " using Jackson", error);
            }
        }
        else {
            logger.log(Level.WARNING, "Cannot write DataContainer " + container + " using JacksonFileDataLoader");
        }
    }
}
