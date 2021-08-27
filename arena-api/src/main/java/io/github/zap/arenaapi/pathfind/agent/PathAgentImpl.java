package io.github.zap.arenaapi.pathfind.agent;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

record PathAgentImpl(double x, double y, double z, double width, double height, double jumpHeight,
                     double fallTolerance) implements PathAgent {
    @Override
    public @NotNull
    BoundingBox getBounds() {
        Vector minCorner = new Vector(x - (width / 2), y, z - (width / 2));
        return BoundingBox.of(minCorner, new Vector(minCorner.getX() + width, minCorner.getY() + height,
                minCorner.getZ() + width));
    }
}
