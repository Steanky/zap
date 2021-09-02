package io.github.zap.arenaapi.pathfind.util;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class Utils {
    private static final WorldBridge worldBridge = ArenaApi.getInstance().getNmsBridge().worldBridge();

    public static double testFall(@NotNull Location location) {
        if(!isValidLocation(location)) {
            return Double.NaN;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        World world = location.getWorld();

        while(y > -1) {
            BlockCollisionView block = worldBridge.collisionFor(world.getBlockAt(x, y, z));

            if(!block.collision().isEmpty()) {
                return location.getY() - block.exactY();
            }

            y--;
        }

        return 0;
    }

    public static boolean isValidLocation(@NotNull Location location) {
        return location.getWorld().getWorldBorder().isInside(location) && location.getY() >= 0 && location.getY() < 256;
    }
}
