package io.github.zap.arenaapi.pathfind;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class EntityAgent<T extends Entity> implements PathAgent {
    private final T entity;
    private final double width;
    private final double height;
    private final Vector vector;

    EntityAgent(@NotNull T entity) {
        this.entity = entity;
        this.width = entity.getWidth();
        this.height = entity.getHeight();
        this.vector = entity.getLocation().toVector();
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

    public T getEntity() {
        return entity;
    }

    @Override
    public String toString() {
        return "EntityAgent{width=" + width + ", height=" + height + ", vector=" + vector + "}";
    }
}
