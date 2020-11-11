package io.github.zap.serialize;

import lombok.Getter;

/**
 * Provides a way to easily pass field-specific converters in an annotation argument.
 */
public enum Converter {
    DEFAULT((object, direction) -> object);

    @Getter
    private final ValueConverter valueConverter;

    Converter(ValueConverter valueConverter) {
        this.valueConverter = valueConverter;
    }
}
