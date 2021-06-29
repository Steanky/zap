package io.github.zap.nms.common.world;

import org.bukkit.ChunkSnapshot;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CollisionChunkSnapshot extends ChunkSnapshot {
    @NotNull BlockCollisionSnapshot collisionSnapshot(int chunkX, int chunkY, int chunkZ);

    boolean collidesWithAny(@NotNull BoundingBox chunkRelativeBounds);

    List<BlockCollisionSnapshot> collisionsWith(@NotNull BoundingBox worldRelativeBounds);
}
