package io.github.zap.arenaapi.pathfind;

import java.util.Comparator;

class ScoreComparator implements Comparator<Score> {
    private static final ScoreComparator instance = new ScoreComparator();

    private ScoreComparator() {}

    static ScoreComparator instance() {
        return instance;
    }

    @Override
    public int compare(Score first, Score second) {
        return Double.compare(first.f, second.f);
    }
}
