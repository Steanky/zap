package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.ImmutableWorldVector;
import io.github.zap.vector.Positional;
import io.github.zap.vector.VectorAccess;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public interface PathDestination extends Positional {
    @NotNull PathTarget target();

    static @NotNull PathDestination fromEntity(@NotNull Entity entity, @NotNull PathTarget target, boolean findBlock) {
        Objects.requireNonNull(entity, "entity cannot be null!");
        return new PathDestinationImpl(findBlock ? vectorOnGround(entity) :
                VectorAccess.immutable(entity.getLocation().toVector()), target);
    }

    static @NotNull PathDestination fromCoordinates(@NotNull PathTarget target,double x, double y, double z) {
        return new PathDestinationImpl(VectorAccess.immutable(x, y, z), target);
    }

    static @NotNull PathDestination fromVector(@NotNull ImmutableWorldVector source, @NotNull PathTarget target) {
        Objects.requireNonNull(source, "source cannot be null!");
        return new PathDestinationImpl(source, target);
    }

    private static ImmutableWorldVector vectorOnGround(Entity entity) {
        Location targetLocation = entity.getLocation();

        int x = targetLocation.getBlockX();
        int y = targetLocation.getBlockY();
        int z = targetLocation.getBlockZ();

        World world = targetLocation.getWorld();
        Block block;

        do {
            block = world.getBlockAt(x, y, z);
        }
        while(block.getType().isEmpty() && --y > -1);

        return VectorAccess.immutable(x, y + 1, z);
    }
}
