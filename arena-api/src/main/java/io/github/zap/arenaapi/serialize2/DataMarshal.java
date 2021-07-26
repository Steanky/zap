package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface DataMarshal {
    /**
     * Creates a data container from the specified map of objects. Will perform a "deep" conversion of the map in order
     * to ensure that anything that is convertible to a DataContainer will be represented as such.
     *
     * This process will mutate the provided map. If this is undesirable, pass in a fresh copy using Map.copyOf().
     * @param mappings The object mappings to use
     * @return An object encapsulating those mappings
     */
    @NotNull DataContainer marshalData(@NotNull Map<String, Object> mappings);

    static DataMarshal from(@NotNull KeyFactory factory) {
        return new StandardDataMarshal(factory);
    }

    static DataMarshal standard() {
        return new StandardDataMarshal(KeyFactory.standard());
    }
}
