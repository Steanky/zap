package io.github.zap.arenaapi.serialize2.jackson;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.OutputStream;

public interface IOSource {
    @NotNull InputStream newInput();

    @NotNull OutputStream newOutput();
}
