package io.github.zap.util;

/**
 * Utility class for things related to math and numbers.
 */
public final class NumberUtils {
    /**
     * Returns true if the target value is within the range specified by minInclusive and maxInclusive
     * @param target The value to test
     * @param minInclusive The lower inclusive bound
     * @param maxInclusive The upper inclusive bound
     * @return Whether or not the target is within the range
     */
    public static boolean inRange(int target, int minInclusive, int maxInclusive) {
        return minInclusive <= target && maxInclusive >= target;
    }
}
