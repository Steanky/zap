package io.github.zap.vector.util;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils {
    public enum Comparison {
        GREATER_THAN,
        LESS_THAN
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static boolean fuzzyComparison(double first, double second, @NotNull Comparison comparison) {
        if(Math.abs(first - second) <= Vector.getEpsilon()) {
            return false;
        }

        return comparison == Comparison.LESS_THAN ? first < second : first > second;
    }
}
