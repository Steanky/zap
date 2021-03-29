package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

class Cost implements Comparable<Cost> {
    public final int nodeCost;
    public final int costToGoal;
    public final int costSum;

    Cost(int nodeCost, int costToGoal) {
        this.nodeCost = nodeCost;
        this.costToGoal = costToGoal;
        this.costSum = nodeCost + costToGoal;
    }

    Cost() {
        this.nodeCost = 0;
        this.costToGoal = 0;
        this.costSum = 0;
    }

    @Override
    public int compareTo(@NotNull Cost o) {
        return Integer.compare(o.costSum, costSum);
    }

    @Override
    public String toString() {
        return "Cost{nodeCost=" + nodeCost + ", costToGoal=" + costToGoal + ", costSum=" + costSum + "}";
    }
}
