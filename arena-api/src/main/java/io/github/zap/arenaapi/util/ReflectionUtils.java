package io.github.zap.arenaapi.util;

import org.apache.commons.lang.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public final class ReflectionUtils {
    /**
     * Searches for an annotation of a given type on a given AnnotatedElement, and returns it if it is found.
     * @param element The element to search
     * @param annotationClass The class of the annotation
     * @param <T> The type of the annotation
     * @return The annotation if it is present; null otherwise
     */
    public static <T extends Annotation> T getDeclaredAnnotation(AnnotatedElement element, Class<T> annotationClass) {
        Annotation[] annotations = element.getDeclaredAnnotations();

        for(Annotation annotation : annotations) {
            if(annotation.annotationType().equals(annotationClass)) {
                //noinspection unchecked
                return (T)annotation;
            }
        }

        return null;
    }
}
