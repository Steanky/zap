package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

class Score {
    public final double g;
    public final double h;
    public final double f;
    private final int hash;

    Score(double g, double h) {
        this.g = g;
        this.h = h;
        this.f = g + h;
        hash = Objects.hash(g, h, f);
    }

    Score() {
        g = Double.POSITIVE_INFINITY;
        h = Double.POSITIVE_INFINITY;
        f = Double.POSITIVE_INFINITY;
        hash = Objects.hash(g, h, f);
    }

    @Override
    public String toString() {
        return "Cost{g=" + g + ", h=" + h + ", f=" + f + "}";
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Score) {
            Score other = (Score)obj;
            return g == other.g && h == other.h && f == other.f;
        }

        return false;
    }
}
