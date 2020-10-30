package io.github.zap.serialize;

public interface ValueConverter {
    ValueConverter DEFAULT = (object, direction) -> object;

    /**
     * Converts the provided object into another object, given a Direction (either serialize or deserialize)
     * @param object The object to convert
     * @param direction Whether the object is being serialized or deserialized
     * @return The converted object
     */
    Object convert(Object object, Direction direction);
}
