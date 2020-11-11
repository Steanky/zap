package io.github.zap.serialize;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class EnumWrapper extends DataSerializable {
    @Getter
    private String enumClass;

    @Getter
    private String enumValue;

    private EnumWrapper() {}
}
