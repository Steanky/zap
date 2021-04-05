package io.github.zap.arenaapi.pathfind;

import io.github.zap.nms.world.WrappedVoxelShape;
import org.bukkit.ChunkSnapshot;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

class DetailedSnapshot {
    private final ChunkSnapshot snapshot;
    private final Map<Long, WrappedVoxelShape> collisionMap = new HashMap<>();

    DetailedSnapshot(@NotNull ChunkSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public @NotNull ChunkSnapshot snapshot() {
        return snapshot;
    }

    public @Nullable WrappedVoxelShape getCollisionFor(int chunkX, int chunkY, int chunkZ) {
        return collisionMap.get(Block.getBlockKey(chunkX, chunkY, chunkZ));
    }
}
