package io.github.zap.zombies.command.mapeditor;

import lombok.Value;

import java.lang.reflect.Field;

@Value
public class EditableField {
    Field field;
    EditorSettable annotation;
}
