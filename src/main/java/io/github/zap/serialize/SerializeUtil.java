package io.github.zap.serialize;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;

/**
 * Utility class for serializable data
 */
public class SerializeUtil {

    /**
     * Runs method on every field of a class that should be serializable
     * @param clazz The class to run the method on
     * @param biConsumer A BiConsumer of the field of the field to
     */
    public static void forEachSerializable(Class<?> clazz, BiConsumer<Field, String> biConsumer) {
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) { // skip static fields
                Annotation[] annotations = field.getDeclaredAnnotations();

                for (Annotation annotation : annotations) {
                    if (annotation.annotationType() == Serialize.class) { // only process @Serialize
                        if (!field.isAccessible()) {
                            field.setAccessible(true); // serialize private members too
                        }

                        Serialize serialize = (Serialize) annotation;

                        biConsumer.accept(field, serialize.name());
                        break;
                    }
                }
            }
        }
    }

}
