package io.github.zap.arenaapi;

public class ObjectDisposedException extends Exception {
    public ObjectDisposedException() {
        super("Object has been disposed.");
    }
}
