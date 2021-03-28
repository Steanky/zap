package io.github.zap.arenaapi.pathfind;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

class EntityPathDestination extends PathDestinationAbstract {
    EntityPathDestination(@NotNull Entity target, boolean findBlock) {
        super(findBlock ? nodeOnGround(target) : nodeAt(target));
    }

    private static PathNode nodeOnGround(Entity entity) {
        Location targetLocation = entity.getLocation();

        int x = targetLocation.getBlockX();
        int y = targetLocation.getBlockY();
        int z = targetLocation.getBlockZ();

        World world = targetLocation.getWorld();
        Block block = world.getBlockAt(x, y, z);

        while(block.getType().isAir() && y > 0) {
            block = world.getBlockAt(x, --y, z);
        }

        return new PathNode(x, ++y, z);
    }

    private static PathNode nodeAt(Entity entity) {
        Location targetLocation = entity.getLocation();
        return new PathNode(targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());
    }
}
