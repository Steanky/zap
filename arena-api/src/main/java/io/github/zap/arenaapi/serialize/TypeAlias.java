package io.github.zap.arenaapi.serialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used by DataSerializable instances to specify a name that will be used to substitute for their fully qualified
 * class name in serialized data.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TypeAlias {
    /**
     * The alias of this class. Cannot be an empty string.
     * @return The alias of this class
     */
    String alias();
}
