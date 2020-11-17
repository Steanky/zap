package io.github.zap.arenaapi.serialize;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Used by the serialization code to allow all enum types to be serialized.
 */
@AllArgsConstructor
public class EnumWrapper extends DataSerializable {
    @Getter
    private String enumClass;

    @Getter
    private String enumValue;

    private EnumWrapper() {}
}
