package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.github.zap.arenaapi.ArenaApi;

import java.io.File;
import java.io.IOException;

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
            writer.writeValue(new File(rootDirectory,filename + EXTENSION), data);
        } catch (IOException e) {
            ArenaApi.warning(String.format("IOException when writing data to file '%s': %s.", filename, e.getMessage()));
        }
    }

    @Override
    public <T> T load(String filename, Class<T> objectClass) {
        try {
            return reader.readValue(new File(rootDirectory,filename + EXTENSION), objectClass);
        } catch (IOException e) {
            ArenaApi.warning(String.format("IOException when reading data from file '%s': %s.", filename, e.getMessage()));
        }

        return null;
    }

    @Override
    public File getRootDirectory() {
        return rootDirectory;
    }

    @Override
    public File getFile(String name) {
        return new File(rootDirectory, name + EXTENSION);
    }

    @Override
    public String getExtension() {
        return EXTENSION;
    }
}
