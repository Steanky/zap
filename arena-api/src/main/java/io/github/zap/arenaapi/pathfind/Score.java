package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents the score of a node. This is used to sort nodes in some sort of collection, which is then used by an A*
 * implementation.
 *
 * Here, g represents the total cumulative distance of the path up to and including the current node. h represents the
 * heuristic, which is generally straight-line distance to the target. f is simply the sum of g and h. Nodes with the
 * lowest f-values will be evaluated first by A*.
 */
public class Score {
    private double g;
    private double h;
    private double f;

    /**
     * Creates a Score object with initial g and h values
     */
    public Score(double g, double h) {
        this.g = g;
        this.h = h;
        this.f = g + h;
    }

    /**
     * Creates a Score object with g, h, and f set to Double.POSITIVE_INFINITY, used to indicate an unexplored node.
     */
    public Score() {
        g = Double.POSITIVE_INFINITY;
        h = Double.POSITIVE_INFINITY;
        f = Double.POSITIVE_INFINITY;
    }

    @Override
    public String toString() {
        return "Cost{g=" + g + ", h=" + h + ", f=" + f + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(g, h, f);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Score other) {
            return g == other.g && h == other.h;
        }

        return false;
    }

    public double getG() {
        return g;
    }

    public double getH() {
        return h;
    }

    public double getF() {
        return f;
    }

    void setG(double g) {
        this.g = g;
        this.f = g + h;
    }

    void setH(double h) {
        this.h = h;
        this.f = g + h;
    }

    void set(double g, double h) {
        this.g = g;
        this.h = h;
        this.f = g + h;
    }
}
