package io.github.zap.arenaapi.pathfind.traversal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class NodeChunk {
    private final NodeGraph parent;
    private final NodeSegment[] segments = new NodeSegment[16];
    private int emptyCount = 16;

    private final int chunkX;
    private final int chunkZ;

    NodeChunk(@NotNull NodeGraph parent, int chunkX, int chunkZ) {
        this.parent = parent;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    void set(int y, @Nullable NodeSegment segment) {
        emptyCount = NodeUtils.setterHelper(segment, segments, y, emptyCount, (a, b) -> parent.removeChunk(chunkX, chunkZ), -1);
    }

    @Nullable NodeSegment get(int y) {
        return segments[y];
    }
}
