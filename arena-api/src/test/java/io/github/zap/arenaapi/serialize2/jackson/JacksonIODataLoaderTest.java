package io.github.zap.arenaapi.serialize2.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Logger;

class JacksonIODataLoaderTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final ByteArrayInputStream inputStream = new ByteArrayInputStream(Json.TEST_JSON.getBytes(StandardCharsets.UTF_8));
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    private final JacksonIODataLoader dataLoader = newLoader(inputStream, outputStream);

    private JacksonIODataLoader newLoader(InputStream inputStream, OutputStream outputStream) {
        return new JacksonIODataLoader(mapper, Logger.getGlobal(),
                IOSource.fromStreams(() -> inputStream, () -> outputStream));
    }

    @Test
    void read() {
        Optional<JacksonDataContainer> container = dataLoader.read();
        Assertions.assertTrue(container.isPresent(), "Cannot load container!");
    }

    @Test
    void write() {
        Optional<JacksonDataContainer> container = dataLoader.read();
        Assertions.assertTrue(container.isPresent());

        JacksonDataContainer first = container.get();
        dataLoader.write(first);

        String resultingOutput = outputStream.toString(StandardCharsets.UTF_8);
        Assertions.assertFalse(resultingOutput.isEmpty());

        ByteArrayInputStream newInput = new ByteArrayInputStream(resultingOutput.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream newOutput = new ByteArrayOutputStream();

        JacksonIODataLoader second = newLoader(newInput, newOutput);
        Optional<JacksonDataContainer> newContainer = second.read();
        Assertions.assertTrue(newContainer.isPresent());

        Assertions.assertEquals(first.node(), newContainer.get().node(),
                "Serialized data does not deserialize to the same object!");
    }
}