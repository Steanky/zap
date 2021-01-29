package io.github.zap.zombies.command.mapeditor;

import org.apache.commons.lang.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EditorSettable {
    String getConverterName() default StringUtils.EMPTY;
}
