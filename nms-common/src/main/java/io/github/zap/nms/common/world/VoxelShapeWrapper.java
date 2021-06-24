package io.github.zap.nms.common.world;

import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public interface VoxelShapeWrapper {
    void forEachBox(@NotNull BoxConsumer consumer);

    @NotNull RayTraceResult raycast(@NotNull Vector start, @NotNull Vector end, @NotNull BlockVector pos);

    double maxHeight();
}
