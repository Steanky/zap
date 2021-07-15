package io.github.zap.arenaapi.pathfind;

import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AgentCharacteristics {
    private static final double DEFAULT_JUMP_HEIGHT = 1.125D;

    private final BoundingBox bounds;
    private final double jumpHeight;
    private final double width;
    private final double height;

    private final int hash;

    public AgentCharacteristics(double width, double height, double jumpHeight) {
        bounds = new BoundingBox(0, 0, 0, width, height, width);
        this.jumpHeight = jumpHeight;
        this.hash = Objects.hash(width, height, jumpHeight);

        this.width  = width;
        this.height = height;
    }

    public AgentCharacteristics(double width, double height) {
        this(width, height, DEFAULT_JUMP_HEIGHT);
    }

    public AgentCharacteristics(@NotNull Entity fromEntity) {
        this(fromEntity.getWidth(), fromEntity.getHeight(), DEFAULT_JUMP_HEIGHT);
    }

    public AgentCharacteristics() {
        this(0, 0, DEFAULT_JUMP_HEIGHT);
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    public double jumpHeight() {
        return jumpHeight;
    }

    public BoundingBox getBounds() {
        return bounds.clone();
    }

    @Override
    public String toString() {
        return "AgentCharacteristics{width=" + width() + ", height=" + height() + "jumpHeight=" + jumpHeight + "}";
    }
}
