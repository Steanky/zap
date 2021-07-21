package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.vector.Vector3I;
import io.github.zap.vector.Vectors;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface PathDestination extends Vector3I {
    @NotNull PathTarget target();

    static @Nullable PathDestination fromEntity(@NotNull Entity entity, @NotNull PathTarget target, boolean findBlock) {
        Objects.requireNonNull(entity, "entity cannot be null!");

        if(findBlock) {
            Vector3I vector = vectorOnGround(entity);

            if(vector == null) {
                return null;
            }

            return new PathDestinationImpl(target, vector.x(), vector.y(), vector.z());
        }

        if(validLocation(entity.getLocation())) {
            Vector vector = entity.getLocation().toVector();
            return new PathDestinationImpl(target, vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
        }

        return null;
    }

    static @Nullable PathDestination fromCoordinates(@NotNull PathTarget target, @NotNull World world, double x, double y, double z) {
        if(validLocation(new Location(world, x, y, z))) {
            return new PathDestinationImpl(target, NumberConversions.floor(x), NumberConversions.floor(y), NumberConversions.floor(z));
        }

        return null;
    }

    static @Nullable PathDestination fromLocation(@NotNull Location source, @NotNull PathTarget target) {
        Objects.requireNonNull(source, "source cannot be null!");
        if(validLocation(source)) {
            return new PathDestinationImpl(target, source.getBlockX(), source.getBlockY(), source.getBlockZ());
        }

        return null;
    }

    private static Vector3I vectorOnGround(Entity entity) {
        Location targetLocation = entity.getLocation();

        if(!validLocation(targetLocation)) {
            return null;
        }

        int x = targetLocation.getBlockX();
        int y = targetLocation.getBlockY();
        int z = targetLocation.getBlockZ();

        World world = targetLocation.getWorld();
        while(y > -1) {
            if(ArenaApi.getInstance().getNmsBridge().worldBridge().blockHasCollision(world.getBlockAt(x, y, z))) {
                return Vectors.of(x, y + 1, z);
            }

            y--;
        }

        return Vectors.asIntFloor(Vectors.of(targetLocation));
    }

    private static boolean validLocation(Location location) {
        return location.getWorld().getWorldBorder().isInside(location) && location.getY() >= 0 && location.getY() < 256;
    }
}
