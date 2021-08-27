package io.github.zap.arenaapi.pathfind.agent;

import io.github.zap.vector.Vector3D;
import io.github.zap.vector.Vectors;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an navigation-capable object. Commonly used to wrap Bukkit objects such as Entity. Provides information
 * which may be critical to determine the 'navigability' of a node using a Characteristics object.
 */
public interface PathAgent extends Vector3D {
    double width();

    double height();

    double jumpHeight();

    double fallTolerance();

    @NotNull BoundingBox getBounds();
}
