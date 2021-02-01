package io.github.zap.zombies.command.mapeditor;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark fields inside of data classes as capable of holding other elements that are themselves editable via
 * the mapeditor.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ChildContainer {
    String constructorName() default StringUtils.EMPTY;
}
