package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.zap.arenaapi.ArenaApi;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;

public class JacksonDataLoader implements DataLoader {
    private final ObjectWriter writer;
    private final ObjectReader reader;

    private final SimpleModule module = new SimpleModule();

    private static final String EXTENSION = "json";

    public JacksonDataLoader() {
        module.addSerializer(Vector.class, new VectorSerializer());
        module.addDeserializer(Vector.class, new VectorDeserializer());

        module.addSerializer(BoundingBox.class, new BoundingBoxSerializer());
        module.addDeserializer(BoundingBox.class, new BoundingBoxDeserializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(module); //register our serializers

        //these settings work better for misc bukkit objects
        objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE));

        writer = objectMapper.writerWithDefaultPrettyPrinter();
        reader = objectMapper.reader();
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

    /**
     * Adds a deserializer to the module
     * @param type The type of the class to deserialize
     * @param deserializer The deserializer itself
     * @param <T> The type of the deserializer
     */
    public <T> void addDeserializer(Class<T> type, JsonDeserializer<? extends T> deserializer) {
        module.addDeserializer(type, deserializer);
    }

    @Override
    public String getExtension() {
        return EXTENSION;
    }
}
