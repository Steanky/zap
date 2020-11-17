package io.github.zap.arenaapi.serialize;

/**
 * General interface for an object that can marshal serialized and deserialized types.
 * @param <T> The type that is about to be serialized (the type of the field)
 * @param <V> The type that was just deserialized (the type retrieved from the serialized data source)
 */
public interface ValueConverter<T,V> {
    /**
     * Performs the necessary serialization conversion.
     * @param object The object to convert
     * @return The marshalled object, which should be serializable
     */
    V serialize(T object);

    /**
     * Performs the necessary deserialization conversion
     * @param object The object that was just retrieved from the serialized data source
     * @return The marshalled object, which must be assignable to the original field
     */
    T deserialize(V object);
}
