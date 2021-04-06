package io.github.zap.nms.common.world;

import org.bukkit.ChunkSnapshot;
import org.jetbrains.annotations.NotNull;

public interface WrappedChunkSnapshot {
    @NotNull ChunkSnapshot snapshot();
}
