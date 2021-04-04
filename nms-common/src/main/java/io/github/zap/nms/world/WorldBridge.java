package io.github.zap.nms.world;

import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

/**
 * Proxies NMS methods relating to worlds.
 */
public interface WorldBridge {
    /**
     * Returns the default world name; i.e. the one defined in server.properties.
     * @return The default world name
     */
    @NotNull String getDefaultWorldName();

    WrappedVoxelShape collisionShape(@NotNull Block block);
}
