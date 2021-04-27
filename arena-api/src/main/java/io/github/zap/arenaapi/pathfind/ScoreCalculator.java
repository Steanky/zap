package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.util.VectorUtils;
import org.jetbrains.annotations.NotNull;

public interface ScoreCalculator {
    ScoreCalculator DISTANCE_ONLY = new ScoreCalculator() {
        @Override
        public double computeG(@NotNull PathfinderContext context, @NotNull PathNode current, @NotNull PathNode to,
                               @NotNull PathDestination destination) {
            return current.score.getG() + VectorUtils.distance(current.x(), current.y(), current.z(), to.x(), to.y(), to.z());
        }

        @Override
        public double computeH(@NotNull PathfinderContext context, @NotNull PathNode current, @NotNull PathDestination destination) {
            return current.distanceSquared(destination.position());
        }
    };

    double computeG(@NotNull PathfinderContext context, @NotNull PathNode from, @NotNull PathNode to, @NotNull PathDestination destination);

    double computeH(@NotNull PathfinderContext context, @NotNull PathNode from, @NotNull PathDestination destination);
}
