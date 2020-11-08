package io.github.zap.serialize;

public interface ValueConverter {
    ValueConverter DEFAULT = (object, direction) -> object;

    /**
     * Converts the provided object into another object during serialization/deserialization.
     * @param object The object to convert
     * @param serializing Whether the object is being serialized. If false, we are deserializing
     * @return The converted object
     */
    Object convert(Object object, boolean serializing);
}
