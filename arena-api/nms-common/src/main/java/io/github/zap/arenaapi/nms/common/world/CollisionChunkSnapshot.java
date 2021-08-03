package io.github.zap.arenaapi.nms.common.world;

import org.bukkit.ChunkSnapshot;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CollisionChunkSnapshot extends ChunkSnapshot {
    @NotNull BlockSnapshot collisionSnapshot(int chunkX, int chunkY, int chunkZ);

    boolean collidesWithAny(@NotNull BoundingBox worldRelativeBounds);

    @NotNull List<BlockSnapshot> collisionsWith(@NotNull BoundingBox worldRelativeBounds);

    int captureTick();
}
