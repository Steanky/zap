package io.github.zap.arenaapi.pathfind;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface PathAgent {
    double getWidth();

    double getHeight();

    World getWorld();

    PathNode nodeAt();

    static PathAgent fromEntity(@NotNull Entity entity) {
        Objects.requireNonNull(entity, "entity cannot be null!");
        return new PathAgent() {
            @Override
            public double getWidth() {
                return entity.getWidth();
            }

            @Override
            public double getHeight() {
                return entity.getHeight();
            }

            @Override
            public World getWorld() {
                return entity.getWorld();
            }

            @Override
            public PathNode nodeAt() {
                return new PathNode(entity.getLocation().toVector());
            }

            @Override
            public String toString() {
                return entity.toString();
            }
        };
    }

    static PathAgent fromLocation(@NotNull Location location, double width, double height) {
        Objects.requireNonNull(location, "location cannot be null!");
        Validate.isTrue(width >= 0, "width cannot be less than 0");
        Validate.isTrue(height >= 0, "height cannot be less than 0");
        Validate.isTrue(Double.isFinite(width), "width must be finite");
        Validate.isTrue(Double.isFinite(height), "height must be finite");

        return new PathAgent() {
            @Override
            public double getWidth() {
                return width;
            }

            @Override
            public double getHeight() {
                return height;
            }

            @Override
            public World getWorld() {
                return location.getWorld();
            }

            @Override
            public PathNode nodeAt() {
                return new PathNode(location.toVector());
            }

            @Override
            public String toString() {
                return "PathAgent{width=" + width + ", height=" + height + ", location=" + location.toString() + "}";
            }
        };
    }
}
