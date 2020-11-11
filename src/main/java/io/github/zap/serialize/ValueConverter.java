package io.github.zap.serialize;

public interface ValueConverter {
    /**
     * Converts the provided object into another object, depending on of it is being serialized or deserialized.
     * @param object The object to convert
     * @param serializing Whether the object is being serialized or deserialized
     * @return The converted object
     */
    Object convert(Object object, boolean serializing);
}
