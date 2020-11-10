package io.github.zap.serialize;

import lombok.Getter;

public enum Converter {
    DEFAULT(ValueConverter.DEFAULT),
    MYTHIC_MOB_SET_CONVERTER((object, direction) -> {
        return object;
    });

    @Getter
    private final ValueConverter converter;

    Converter(ValueConverter converter) {
        this.converter = converter;
    }
}
