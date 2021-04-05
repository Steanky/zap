package io.github.zap.arenaapi.pathfind;

import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public interface PathDestination {
    @NotNull PathNode node();

    double destinationScore(@NotNull PathNode from);

    static @NotNull PathDestination fromEntity(@NotNull Entity entity, boolean findBlock) {
        Objects.requireNonNull(entity, "entity cannot be null!");
        return new PathDestinationImpl(findBlock ? nodeOnGround(entity) : new PathNode(entity));
    }

    static @NotNull PathDestination fromCoordinates(int x, int y, int z) {
        return new PathDestinationImpl(new PathNode(x, y, z));
    }

    static @NotNull PathDestination fromVector(@NotNull Vector vector) {
        Objects.requireNonNull(vector, "vector cannot be null!");
        return new PathDestinationImpl(new PathNode(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()));
    }

    static @NotNull Set<PathDestination> fromEntities(@NotNull Collection<? extends Entity> entities, boolean findBlocks) {
        Objects.requireNonNull(entities, "entities cannot be null!");
        Validate.isTrue(entities.size() > 0, "entities collection cannot be empty");

        Set<PathDestination> destinations = new HashSet<>();

        for(Entity entity : entities) {
            destinations.add(new PathDestinationImpl(findBlocks ? nodeOnGround(entity) : new PathNode(entity)));
        }

        return destinations;
    }

    private static PathNode nodeOnGround(Entity entity) {
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

        return new PathNode(x, ++y, z);
    }
}
