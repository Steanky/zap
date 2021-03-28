package io.github.zap.arenaapi.pathfind;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface PathDestination {
    @NotNull PathNode targetNode();

    static @NotNull PathDestination fromEntity(@NotNull Entity entity, boolean findBlock) {
        return new EntityPathDestination(Objects.requireNonNull(entity, "entity cannot be null!"), findBlock);
    }

    static @NotNull PathDestination fromCoordinates(int x, int y, int z) {
        return new BlockPathDestination(x, y, z);
    }

    static @NotNull PathDestination fromVector(@NotNull Vector vector) {
        Objects.requireNonNull(vector, "vector cannot be null!");
        return new BlockPathDestination(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    static @NotNull List<PathDestination> fromEntities(boolean findBlocks, Entity... entities) {
        List<PathDestination> destinations = new ArrayList<>();

        for(Entity entity : entities) {
            destinations.add(new EntityPathDestination(entity, findBlocks));
        }

        return destinations;
    }
}
