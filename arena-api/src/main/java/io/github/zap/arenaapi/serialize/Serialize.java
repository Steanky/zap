package io.github.zap.arenaapi.serialize;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to provided additional information to the serializer.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Serialize {
    /**
     * The name that this field's value should be serialized under.
     * @return The name that this field's value should be serialized under. If this equals an empty string, the name
     * of the field should be used instead.
     */
    String name() default StringUtils.EMPTY;

    /**
     * The name of the converter used to convert this instance, or an empty string if no conversion should be performed.
     * @return The name of the converter to use, or an empty string
     */
    String converter() default StringUtils.EMPTY;

    /**
     * Whether or not the field is an <i>aggregation</i> (subclass of Map or Collection) and the implementation requires
     * a deep conversion of the data structure (lists of arrays, or collections containing types for which
     * ConfigurationSerializable cannot natively handle).
     * @return Whether or not the field is an aggregation. Defaults to false
     */
    boolean isAggregation() default false;

    /**
     * Whether or not to skip serializing this element. Defaults to false.
     * @return True if this member should be skipped, false otherwise
     */
    boolean skip() default false;
}
