package io.github.zap.util;

import io.github.zap.game.MultiBoundingBox;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public final class WorldUtils {
    public static Block getBlockAt(World world, Vector vector) {
        return world.getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public static void fillBounds(World world, BoundingBox bounds, Material material) {
        int minX = (int)bounds.getMinX();
        int minY = (int)bounds.getMinX();
        int minZ = (int)bounds.getMinX();

        int maxX = (int)bounds.getMaxX();
        int maxY = (int)bounds.getMaxX();
        int maxZ = (int)bounds.getMaxX();

        for(int x = minX; x < maxX; x++) {
            for(int y = minY; y < maxY; y++) {
                for(int z = minZ; z < maxZ; z++) {
                    world.getBlockAt(x, y, z).setType(material);
                }
            }
        }
    }

    public static void fillBounds(World world, MultiBoundingBox bounds, Material material) {
        for(BoundingBox box : bounds.getBounds()) {
            fillBounds(world, box, material);
        }
    }
}
