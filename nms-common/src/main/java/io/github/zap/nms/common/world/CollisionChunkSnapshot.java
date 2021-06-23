package io.github.zap.nms.common.world;

import org.bukkit.ChunkSnapshot;
import org.jetbrains.annotations.Nullable;

public interface CollisionChunkSnapshot extends ChunkSnapshot {
    @Nullable BlockCollisionSnapshot blockCollisionSnapshot(int chunkX, int chunkY, int chunkZ);
}
