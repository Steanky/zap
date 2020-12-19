package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.zap.arenaapi.ArenaApi;

import java.io.File;
import java.io.IOException;

public class JacksonDataLoader implements DataLoader {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
    private final ObjectReader reader = objectMapper.reader();

    @Override
    public void save(Object data, File file) {
        try {
            writer.writeValue(file, data);
        } catch (IOException e) {
            ArenaApi.warning("IOException when writing data to file.");
        }
    }

    @Override
    public <T> T load(File file, Class<T> objectClass) {
        try {
            return reader.readValue(file, objectClass);
        } catch (IOException e) {
            ArenaApi.warning("IOException when reading data from file.");
        }

        return null;
    }
}
