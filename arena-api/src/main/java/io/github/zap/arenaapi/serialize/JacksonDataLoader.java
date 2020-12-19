package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.core.JsonParser;
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
    public void save(Object data, File file, String name) {
        try {
            ObjectNode node = (ObjectNode)objectMapper.readTree(file);
            node.set(name, JsonNodeFactory.instance.pojoNode(data));

            writer.writeValue(file, node);
        } catch (IOException e) {
            ArenaApi.warning("IOException when writing data to file.");
        }
    }

    @Override
    public <T> T load(File file, Class<T> objectClass, String name) {
        try {
            JsonNode node = objectMapper.readTree(file);
            JsonNode target = node.findValue(name);

            if(target != null) {
                return reader.readValue(target, objectClass);
            }
            else {
                ArenaApi.warning(String.format("Node key %s does not exist.", name));
            }
        } catch (IOException e) {
            ArenaApi.warning("IOException when reading data from file.");
        }

        return null;
    }
}
