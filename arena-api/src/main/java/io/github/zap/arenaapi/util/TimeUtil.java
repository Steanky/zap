package io.github.zap.arenaapi.util;

/**
 * Utils for ticks or time
 */
public class TimeUtil {

    /**
     * Converts a tick count to seconds as a string
     * @param ticks The tick count
     * @return A string representation of the seconds remaining
     */
    public static String convertTicksToSecondsString(long ticks) {
        return String.format("%.2fs", (double) (ticks / 20) + 0.05D * (ticks % 20));
    }

}
