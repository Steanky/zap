package io.github.zap.arenaapi.pathfind.operation;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

class NodeComparator implements Comparator<PathNodeImpl> {
    private static final NodeComparator INSTANCE = new NodeComparator();
    private static final ScoreComparator SCORE_COMPARATOR = ScoreComparator.instance();

    private NodeComparator() {}

    public static NodeComparator instance() {
        return INSTANCE;
    }

    @Override
    public int compare(@NotNull PathNodeImpl first, @NotNull PathNodeImpl second) {
        return SCORE_COMPARATOR.compare(first.score, second.score);
    }
}
