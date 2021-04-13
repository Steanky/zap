package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.WorldVectorSource;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public interface PathDestination {
    @NotNull WorldVectorSource position();

    static @NotNull PathDestination fromEntity(@NotNull Entity entity, boolean findBlock) {
        Objects.requireNonNull(entity, "entity cannot be null!");
        return new PathDestinationImpl(findBlock ? vectorOnGround(entity) :
                new WorldVectorSource(entity.getLocation().toVector()));
    }

    static @NotNull PathDestination fromCoordinates(int x, int y, int z) {
        return new PathDestinationImpl(new WorldVectorSource(x, y, z));
    }

    static @NotNull PathDestination fromSource(@NotNull WorldVectorSource source) {
        Objects.requireNonNull(source, "source cannot be null!");
        return new PathDestinationImpl(source);
    }

    static @NotNull Set<PathDestination> fromEntities(@NotNull Collection<? extends Entity> entities, boolean findBlocks) {
        Objects.requireNonNull(entities, "entities cannot be null!");
        Validate.isTrue(entities.size() > 0, "entities collection cannot be empty");

        Set<PathDestination> destinations = new HashSet<>();

        for(Entity entity : entities) {
            destinations.add(new PathDestinationImpl(findBlocks ? vectorOnGround(entity) :
                    new WorldVectorSource(entity.getLocation().toVector())));
        }

        return destinations;
    }

    private static WorldVectorSource vectorOnGround(Entity entity) {
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

        return new WorldVectorSource(x, ++y, z);
    }
}
