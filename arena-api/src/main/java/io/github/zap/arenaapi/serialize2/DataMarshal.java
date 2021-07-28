package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Contains methods useful for converting raw deserialized data (in the form of a map of Strings and Objects) to the
 * DataContainer API
 */
public interface DataMarshal extends ConverterRegistry {
    /**
     * Creates a data container from the specified map of objects. Will perform a "deep" conversion of the map in order
     * to ensure that anything that is convertible to a DataContainer will be represented as such.
     *
     * This process will mutate the provided map. If this is undesirable, pass in a fresh copy using Map.copyOf().
     * @param mappings The object mappings to use
     * @return An object encapsulating those mappings
     */
    @NotNull DataContainer fromMappings(@NotNull Map<String, Object> mappings);

    /**
     * Creates a mapping of objects from a given DataContainer. This is essentially the inversion of what fromMappings
     * does, although it is not guaranteed that map.equals(toMappings(fromMappings(map))).
     *
     * This process should not mutate the DataContainer.
     * @param container The container to convert to mappings
     * @return A Map of strings and objects
     */
    @NotNull Map<String, Object> toMappings(@NotNull DataContainer container);

    static DataMarshal from(@NotNull KeyFactory factory) {
        return new StandardDataMarshal(factory);
    }
}
