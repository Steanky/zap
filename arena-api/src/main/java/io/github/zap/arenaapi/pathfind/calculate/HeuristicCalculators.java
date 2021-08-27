package io.github.zap.arenaapi.pathfind.calculate;

import io.github.zap.vector.Vectors;
import org.jetbrains.annotations.NotNull;

public final class HeuristicCalculators {
    public static @NotNull HeuristicCalculator distanceOnly() {
        return (context, current, destination) -> Vectors.distance(current, destination);
    }
}
