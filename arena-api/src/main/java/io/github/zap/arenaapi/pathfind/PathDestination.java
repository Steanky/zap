package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.vector.Vector3I;
import io.github.zap.vector.Vectors;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface PathDestination extends Vector3I {
    @NotNull PathTarget target();

    static @NotNull PathDestination fromEntity(@NotNull Entity entity, @NotNull PathTarget target, boolean findBlock) {
        Objects.requireNonNull(entity, "entity cannot be null!");

        if(findBlock) {
            Vector3I vector = vectorOnGround(entity);
            return new PathDestinationImpl(target, vector.x(), vector.y(), vector.z());
        }

        Vector vector = entity.getLocation().toVector();
        return new PathDestinationImpl(target, vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    static @NotNull PathDestination fromCoordinates(@NotNull PathTarget target, double x, double y, double z) {
        return new PathDestinationImpl(target, (int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
    }

    static @NotNull PathDestination fromVector(@NotNull Vector source, @NotNull PathTarget target) {
        Objects.requireNonNull(source, "source cannot be null!");
        return new PathDestinationImpl(target, source.getBlockX(), source.getBlockY(), source.getBlockZ());
    }

    private static Vector3I vectorOnGround(Entity entity) {
        Location targetLocation = entity.getLocation();

        int x = targetLocation.getBlockX();
        int y = targetLocation.getBlockY();
        int z = targetLocation.getBlockZ();

        World world = targetLocation.getWorld();
        Block block;

        do {
            block = world.getBlockAt(x, y, z);
        }
        while(--y > -1 && ArenaApi.getInstance().getNmsBridge().worldBridge().blockHasCollision(block));

        return Vectors.of(x, y + 1, z);
    }
}
