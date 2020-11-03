package io.github.zap.util;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public final class WorldUtils {
    public static Block getBlockAt(World world, Vector vector) {
        return world.getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }
}
