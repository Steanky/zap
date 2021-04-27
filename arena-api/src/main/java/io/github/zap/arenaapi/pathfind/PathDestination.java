package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.ImmutableWorldVector;
import io.github.zap.arenaapi.vector.Positional;
import io.github.zap.arenaapi.vector.VectorAccess;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public interface PathDestination extends Positional {
    static @NotNull PathDestination fromEntity(@NotNull Entity entity, boolean findBlock) {
        Objects.requireNonNull(entity, "entity cannot be null!");
        return new PathDestinationImpl(findBlock ? vectorOnGround(entity) : VectorAccess.immutable(entity.getLocation().toVector()));
    }

    static @NotNull PathDestination fromCoordinates(double x, double y, double z) {
        return new PathDestinationImpl(VectorAccess.immutable(x, y, z));
    }

    static @NotNull PathDestination fromVector(@NotNull ImmutableWorldVector source) {
        Objects.requireNonNull(source, "source cannot be null!");
        return new PathDestinationImpl(source);
    }

    static @NotNull Set<PathDestination> fromEntities(@NotNull Collection<? extends Entity> entities, boolean findBlocks) {
        Objects.requireNonNull(entities, "entities cannot be null!");
        Validate.isTrue(entities.size() > 0, "entities collection cannot be empty");

        Set<PathDestination> destinations = new HashSet<>();

        for(Entity entity : entities) {
            destinations.add(new PathDestinationImpl(findBlocks ? vectorOnGround(entity) :
                    VectorAccess.immutable(entity.getLocation().toVector())));
        }

        return destinations;
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
        while(block.getType().isAir() && --y > -1);

        return VectorAccess.immutable(x, ++y, z);
    }
}
