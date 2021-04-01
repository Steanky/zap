package io.github.zap.arenaapi.pathfind;

import java.util.Comparator;

class NodeComparator implements Comparator<PathNode> {
    private static final NodeComparator INSTANCE = new NodeComparator();
    private static final ScoreComparator SCORE_COMPARATOR = ScoreComparator.instance();

    private NodeComparator() {}

    static NodeComparator instance() {
        return INSTANCE;
    }

    @Override
    public int compare(PathNode first, PathNode second) {
        int scoreComparison = SCORE_COMPARATOR.compare(first.score, second.score);
        if(scoreComparison == 0) {
            int xComparison = Integer.compare(first.x, second.x);
            if(xComparison == 0) {
                int yComparison = Integer.compare(first.y, second.y);
                if(yComparison == 0) {
                    return Integer.compare(first.z, second.z);
                }

                return yComparison;
            }

            return xComparison;
        }

        return scoreComparison;
    }
}
