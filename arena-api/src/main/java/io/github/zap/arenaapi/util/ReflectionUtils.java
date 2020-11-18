package io.github.zap.arenaapi.util;

import org.apache.commons.lang.StringUtils;

import java.lang.annotation.Annotation;

public final class ReflectionUtils {
    private static final String CLASS_NAME = ReflectionUtils.class.getName();

    /**
     * Gets the class that called the function in which this method was invoked.
     * @return The class that called the function in which this method was invoked
     */
    public static String getCallerName() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();

        for(int i = 2; i < elements.length; i++) { //skip first two entries
            StackTraceElement element = elements[i];
            String elementName = element.getClassName();

            if(!elementName.equals(CLASS_NAME)) {
                return elementName;
            }
        }

        return null; //may return this under very odd circumstances
    }

    public static <T extends Annotation> T getDeclaredAnnotation(Class<?> targetClass, Class<T> annotationClass) {
        Annotation[] annotations = targetClass.getDeclaredAnnotations();

        for(Annotation annotation : annotations) {
            if(annotation.annotationType().equals(annotationClass)) {
                //noinspection unchecked
                return (T)annotation;
            }
        }

        return null;
    }
}
