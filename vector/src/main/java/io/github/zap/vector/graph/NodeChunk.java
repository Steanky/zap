package io.github.zap.vector.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

class NodeChunk {
    private final NodeSegment[] segments = new NodeSegment[16];
    private int emptyCount = 16;

    private final int chunkX;
    private final int chunkZ;

    private final BiConsumer<Integer, Integer> chunkRemover;

    NodeChunk(int chunkX, int chunkZ, @NotNull BiConsumer<Integer, Integer> chunkRemover) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.chunkRemover = chunkRemover;
    }

    void set(int y, @Nullable NodeSegment segment) {
        emptyCount = NodeUtils.setterHelper(segment, segments, y, emptyCount, (a, b) ->
                chunkRemover.accept(chunkX, chunkZ), -1);
    }

    @Nullable NodeSegment get(int y) {
        return segments[y];
    }
}
