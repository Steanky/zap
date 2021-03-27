package io.github.zap.arenaapi.pathfind;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

class EntityPathDestination implements PathDestination {
    private final PathNode node;

    EntityPathDestination(@NotNull Entity target) {
        Location targetLocation = target.getLocation();

        int x = targetLocation.getBlockX();
        int y = targetLocation.getBlockY();
        int z = targetLocation.getBlockZ();

        World world = targetLocation.getWorld();
        Block block = world.getBlockAt(x, y, z);

        while(block.getType().isAir() && y > 0) {
            block = world.getBlockAt(x, --y, z);
        }

        node = new PathNode(x, y, z);
    }

    @Override
    public @NotNull PathNode targetNode() {
        return node;
    }
}
