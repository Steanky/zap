package io.github.zap.arenaapi.pathfind;

import java.util.Objects;

public class Score {
    public enum Delta {
        UNCHANGED,
        INCREASE,
        DECREASE
    }

    private double g;
    private double h;
    private double f;
    private Delta deltaG = Delta.UNCHANGED;
    private int hash;

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

    public double getG() {
        return g;
    }

    public double getH() {
        return h;
    }

    public double getF() {
        return f;
    }

    public Delta deltaG() {
        return deltaG;
    }

    void setG(double g) {
        if(this.g > g) {
            deltaG = Delta.DECREASE;
        }
        else if(this.g < g) {
            deltaG = Delta.INCREASE;
        }
        else{
            deltaG = Delta.UNCHANGED;
        }

        this.g = g;
        this.f = g + h;
        hash = Objects.hash(g, h, f);
    }

    void setH(double h) {
        this.h = h;
        this.f = g + h;
        hash = Objects.hash(g, h, f);
    }

    void set(double g, double h) {
        this.g = g;
        this.h = h;
        this.f = g + h;
        hash = Objects.hash(g, h, f);
    }
}
