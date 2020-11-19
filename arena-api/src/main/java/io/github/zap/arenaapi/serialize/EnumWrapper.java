package io.github.zap.arenaapi.serialize;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Used by the serialization code to allow all enum types to be serialized.
 */
@AllArgsConstructor
@TypeAlias("WrappedEnum")
public class EnumWrapper extends DataSerializable {
    @Getter
    private String enumClass;

    @Getter
    private String enumValue;

    private EnumWrapper() {}
}
