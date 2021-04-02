package io.github.zap.arenaapi.pathfind;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class EntityAgent<T extends Entity> implements PathAgent {
    private final T entity;
    private final Characteristics characteristics;
    private final Vector vector;

    EntityAgent(@NotNull T entity) {
        this.entity = entity;
        characteristics = new Characteristics(entity.getWidth(), entity.getHeight());
        this.vector = entity.getLocation().toVector();
    }

    @Override
    public Characteristics characteristics() {
        return characteristics;
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
        return "EntityAgent{characteristics=" + characteristics + ", vector=" + vector + "}";
    }
}
