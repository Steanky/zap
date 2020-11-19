package io.github.zap.arenaapi.serialize;

import lombok.Value;

import java.lang.reflect.Field;

@Value
public class SerializationEntry {
    String name;
    Field field;
    ValueConverter<?,?> converter;
    boolean collection;
}
