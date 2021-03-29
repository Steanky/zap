package io.github.zap.arenaapi.pathfind;

import java.util.Comparator;

class NodeComparator implements Comparator<PathNode> {
    private static final NodeComparator instance = new NodeComparator();

    private NodeComparator() {}

    static NodeComparator instance() {
        return instance;
    }

    @Override
    public int compare(PathNode first, PathNode second) {
        int scoreComparison = ScoreComparator.instance().compare(first.score, second.score);
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
