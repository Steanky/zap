package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.zap.arenaapi.ArenaApi;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;

public class JacksonDataLoader implements DataLoader {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
    private final ObjectReader reader = objectMapper.reader();

    private static final String EXTENSION = "json";

    public JacksonDataLoader() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Vector.class, new VectorDeserializerModifier());
        module.addSerializer(Vector.class, new VectorSerializerModifier());

        objectMapper.registerModule(module);

        try {
            objectMapper.writeValueAsString(new Vector());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Object data, File file) {
        try {
            writer.writeValue(file, data);
        } catch (IOException e) {
            ArenaApi.warning(String.format("IOException when writing data to file: %s.", e.getMessage()));
        }
    }

    @Override
    public <T> T load(File file, Class<T> objectClass) {
        try {
            return reader.readValue(file, objectClass);
        } catch (IOException e) {
            ArenaApi.warning(String.format("IOException when reading data from file: %s.", e.getMessage()));
        }

        return null;
    }

    @Override
    public String getExtension() {
        return EXTENSION;
    }
}
