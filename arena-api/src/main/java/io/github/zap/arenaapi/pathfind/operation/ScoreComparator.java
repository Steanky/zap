package io.github.zap.arenaapi.pathfind.operation;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class ScoreComparator implements Comparator<Score> {
    private static final ScoreComparator instance = new ScoreComparator();

    private ScoreComparator() {}

    public static ScoreComparator instance() {
        return instance;
    }

    @Override
    public int compare(@NotNull Score first, @NotNull Score second) {
        return Double.compare(first.getF(), second.getF());
    }
}
