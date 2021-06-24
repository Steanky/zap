package io.github.zap.nms.common.world;

import org.bukkit.ChunkSnapshot;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public interface CollisionChunkSnapshot extends ChunkSnapshot {
    @NotNull BlockCollisionSnapshot blockCollisionSnapshot(int chunkX, int chunkY, int chunkZ);

    boolean collidesWithAny(@NotNull BoundingBox bounds);
}
