package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.github.zap.arenaapi.ArenaApi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class JacksonDataLoader implements DataLoader {
    private static final String EXTENSION = ".json";

    private final ObjectWriter writer;
    private final ObjectReader reader;

    private final File rootDirectory;

    public JacksonDataLoader(File rootDirectory) {
        ObjectMapper mapper = ArenaApi.getInstance().getMapper();

        writer = mapper.writerWithDefaultPrettyPrinter();
        reader = mapper.reader();
        this.rootDirectory = rootDirectory;

        //noinspection ResultOfMethodCallIgnored
        rootDirectory.mkdirs();
    }

    @Override
    public void save(Object data, String filename) {
        try {
            writer.writeValue(new File(filename + EXTENSION), data);
        } catch (IOException e) {
            ArenaApi.warning(String.format("IOException when writing data to file: %s.", e.getMessage()));
        }
    }

    @Override
    public <T> T load(String filename, Class<T> objectClass) {
        try {
            return reader.readValue(new File(filename + EXTENSION), objectClass);
        } catch (IOException e) {
            ArenaApi.warning(String.format("IOException when reading data from file: %s.", e.getMessage()));
        }

        return null;
    }

    @Override
    public File getRootDirectory() {
        return rootDirectory;
    }

    @Override
    public File getFile(String name) {
        return Path.of(rootDirectory.getPath(), name + EXTENSION).toFile();
    }

    @Override
    public String getExtension() {
        return EXTENSION;
    }
}
