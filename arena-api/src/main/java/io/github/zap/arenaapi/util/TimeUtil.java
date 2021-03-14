package io.github.zap.arenaapi.util;

/**
 * Utils for ticks or time
 */
public class TimeUtil {

    /**
     * Converts a tick count to seconds
     * @param ticks The tick count
     * @return A double representation of the seconds remaining
     */
    public static double convertTicksToSeconds(long ticks) {
        return (double) (ticks / 20) + 0.05D * (ticks % 20);
    }

}
