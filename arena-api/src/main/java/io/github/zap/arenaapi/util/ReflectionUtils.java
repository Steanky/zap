package io.github.zap.arenaapi.util;

import org.apache.commons.lang.StringUtils;

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

        return StringUtils.EMPTY; //may return this under very odd circumstances
    }
}
