package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

class ScoreComparator implements Comparator<Score> {
    private static final ScoreComparator instance = new ScoreComparator();

    private ScoreComparator() {}

    static ScoreComparator instance() {
        return instance;
    }

    @Override
    public int compare(@NotNull Score first, @NotNull Score second) {
        return Double.compare(first.getF(), second.getF());
    }
}
