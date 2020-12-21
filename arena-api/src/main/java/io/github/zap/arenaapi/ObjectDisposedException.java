package io.github.zap.arenaapi;

public class ObjectDisposedException extends RuntimeException {
    public ObjectDisposedException() {
        super("Object has been disposed.");
    }
}
