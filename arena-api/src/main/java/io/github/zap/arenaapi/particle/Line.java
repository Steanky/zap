package io.github.zap.arenaapi.particle;

import org.bukkit.util.Vector;

public class Line implements VectorProvider {
    private final Vector initialStart;
    private final Vector initialEnd;
    private final double density;

    private int length = -1;
    private Vector start;
    private Vector step;

    public Line(Vector start, Vector end, double density) {
        this.initialStart = start.clone();
        this.initialEnd = end.clone();
        this.density = density;
    }

    @Override
    public int init() {
        if(length == -1) {
            length = (int)Math.round(initialStart.distance(initialEnd) * density);
            start = initialStart.clone();
            step = initialEnd.clone().subtract(initialStart).multiply(1D / (double)length);
        }

        return length;
    }

    @Override
    public Vector next() {
        Vector vector = start.clone();
        start.add(step);
        return vector;
    }

    @Override
    public void reset() {
        start = initialStart.clone();
    }
}
