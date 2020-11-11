package io.github.zap.serialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation should be applied to any collection that is supposed to be deeply serialized; ex. all of its objects
 * will be iterated and converted if necessary.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SerializeCollection {
}
