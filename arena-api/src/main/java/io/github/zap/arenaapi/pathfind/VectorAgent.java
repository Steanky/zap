package io.github.zap.arenaapi.pathfind;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class VectorAgent implements PathAgent {
    private final Vector vector;
    private final double width;
    private final double height;

    VectorAgent(@NotNull Vector vector, double width, double height) {
        this.vector = vector.clone();
        this.width = width;
        this.height = height;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public PathNode nodeAt() {
        return new PathNode(vector);
    }

    @Override
    public Vector position() {
        return vector.clone();
    }
}
