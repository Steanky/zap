package io.github.zap.arenaapi.util;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AggregateException extends RuntimeException {
    private final List<Exception> exceptions;

    public AggregateException(@NotNull String message, @NotNull List<Exception> exceptions) {
        super(message);
        this.exceptions = exceptions;
    }

    public @NotNull List<Exception> getExceptions() {
        return exceptions;
    }
}
