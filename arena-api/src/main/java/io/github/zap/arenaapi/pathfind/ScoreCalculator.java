package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.util.VectorUtils;
import org.jetbrains.annotations.NotNull;

public interface ScoreCalculator {
    ScoreCalculator DISTANCE = new ScoreCalculator() {
        @Override
        public double computeG(@NotNull PathfinderContext context, @NotNull PathNode current, @NotNull PathNode to, @NotNull PathDestination destination) {
            return current.score.g + VectorUtils.distance(current.x, current.y, current.z, to.x, to.y, to.z);
        }

        @Override
        public double computeH(@NotNull PathfinderContext context, @NotNull PathNode current, @NotNull PathDestination destination) {
            return destination.destinationScore(current);
        }
    };

    double computeG(@NotNull PathfinderContext context, @NotNull PathNode from, @NotNull PathNode to, @NotNull PathDestination destination);

    double computeH(@NotNull PathfinderContext context, @NotNull PathNode from, @NotNull PathDestination destination);
}