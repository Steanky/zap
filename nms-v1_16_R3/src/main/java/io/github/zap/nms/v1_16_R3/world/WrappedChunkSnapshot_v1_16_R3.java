package io.github.zap.nms.v1_16_R3.world;

import io.github.zap.nms.common.Wrapper;
import io.github.zap.nms.common.world.WrappedChunkSnapshot;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunkSnapshot;
import org.jetbrains.annotations.NotNull;

class WrappedChunkSnapshot_v1_16_R3 extends Wrapper<CraftChunkSnapshot> implements WrappedChunkSnapshot {
    WrappedChunkSnapshot_v1_16_R3(@NotNull Chunk chunk) {
        super((CraftChunkSnapshot) chunk.getChunkSnapshot());
    }

    @Override
    public @NotNull ChunkSnapshot snapshot() {
        return wrappedObject;
    }
}
