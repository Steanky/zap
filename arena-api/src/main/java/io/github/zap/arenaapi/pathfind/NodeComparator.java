package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class NodeComparator implements Comparator<PathNode> {
    private static final NodeComparator INSTANCE = new NodeComparator();
    private static final ScoreComparator SCORE_COMPARATOR = ScoreComparator.instance();

    private NodeComparator() {}

    public static NodeComparator instance() {
        return INSTANCE;
    }

    @Override
    public int compare(@NotNull PathNode first, @NotNull PathNode second) {
        if(first == null || second == null) {
            ArenaApi.warning("t");
        }
        return SCORE_COMPARATOR.compare(first.score, second.score);
    }
}
