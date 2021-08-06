package io.github.zap.arenaapi.serialize2.jackson;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * Represents a simple source of input and output.
 */
public interface IOSource {
    /**
     * Creates a new InputStream from this IOSource.
     *
     * Since the stream may be holding on to some resource, it is important to call close() on it after it has been
     * used.
     * @return A new InputStream for this IOSource
     * @throws IOException When the underlying stream creation throws an IOException
     */
    @NotNull InputStream newInput() throws IOException;

    /**
     * Creates a new OutputStream from this IOSource.
     *
     * Since the stream may be holding on to some resource, it is important to call close() on it after it has been
     * used.
     * @return A new OutputStream for this IOSource
     * @throws IOException When the underlying stream creation throws an IOException
     */
    @NotNull OutputStream newOutput() throws IOException;

    static @NotNull IOSource fromFile(@NotNull File file) {
        return new IOSource() {
            @Override
            public @NotNull InputStream newInput() throws FileNotFoundException {
                return new FileInputStream(file);
            }

            @Override
            public @NotNull OutputStream newOutput() throws FileNotFoundException {
                return new FileOutputStream(file);
            }
        };
    }

    static @NotNull IOSource fromString(@NotNull String string) {
        return new IOSource() {
            @Override
            public @NotNull InputStream newInput() {
                return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public @NotNull OutputStream newOutput() {
                return OutputStream.nullOutputStream();
            }
        };
    }

    static @NotNull IOSource fromStreams(@NotNull Supplier<InputStream> inputStream, @NotNull Supplier<OutputStream> outputStream) {
        return new IOSource() {
            @Override
            public @NotNull InputStream newInput() {
                return inputStream.get();
            }

            @Override
            public @NotNull OutputStream newOutput() {
                return outputStream.get();
            }
        };
    }
}
