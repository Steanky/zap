package io.github.zap.arenaapi.serialize2.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.github.zap.arenaapi.serialize2.IOSource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JacksonIODataLoader extends JacksonDataLoaderAbstract {
    private final IOSource io;

    private final ObjectWriter writer;

    public JacksonIODataLoader(@NotNull ObjectMapper mapper, @NotNull Logger logger, @NotNull IOSource io) {
        super(mapper, logger);
        this.io = io;
        this.writer = mapper.writer();
    }

    @Override
    public @NotNull Optional<JacksonDataContainer> read() {
        try {
            JsonNode node;
            try(InputStream input = io.newInput()) {
                node = mapper.readTree(input);

                if(node != null) {
                    return Optional.of(new JacksonDataContainer(mapper, node));
                }
                else {
                    logger.log(Level.WARNING, "No context found for JSON data: end-of-input");
                }
            }
        } catch (IOException error) {
            logger.log(Level.WARNING, "IOException occurred while parsing JSON data from input stream using Jackson", error);
        }

        return Optional.empty();
    }

    @Override
    public void write(@NotNull JacksonDataContainer container) {
        try {
            try (OutputStream output = io.newOutput()) {
                writer.writeValue(output, container.node());
            }
        } catch (IOException error) {
            logger.log(Level.WARNING, "IOException occurred while writing JSON data to output stream using Jackson", error);
        }
    }
}
